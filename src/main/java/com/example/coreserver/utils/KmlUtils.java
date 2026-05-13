package com.example.coreserver.utils;

import com.example.coreserver.entity.Geofence;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class KmlUtils {
    private static final String KML_NS = "http://www.opengis.net/kml/2.2";
    private static final double METERS_PER_DEGREE = 111319.9; // 1度≈111319.9米（赤道近似值）

    // 导出禁飞区列表为KML字符串
    public static String exportToKml(List<Geofence> geofences) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            // 创建根元素
            Element kml = doc.createElementNS(KML_NS, "kml");
            doc.appendChild(kml);

            Element document = doc.createElement("Document");
            kml.appendChild(document);

            // 为每个禁飞区生成Placemark
            for (Geofence gf : geofences) {
                document.appendChild(createPlacemark(doc, gf));
            }

            // 转换为字符串
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(doc), new StreamResult(output));

            return output.toString();
        } catch (Exception e) {
            throw new RuntimeException("生成KML失败", e);
        }
    }

    // 创建单个禁飞区的Placemark元素
    private static Element createPlacemark(Document doc, Geofence gf) {
        Element placemark = doc.createElement("Placemark");

        // 名称
        Element name = doc.createElement("name");
        name.setTextContent(gf.getName());
        placemark.appendChild(name);

        // 扩展数据（存储半径参数）
        Element extendedData = doc.createElement("ExtendedData");
        addData(doc, extendedData, "coreRadius", String.valueOf(gf.getCoreRadius()));
        addData(doc, extendedData, "bufferRadius", String.valueOf(gf.getBufferRadius()));
        addData(doc, extendedData, "alertRadius", String.valueOf(gf.getAlertRadius()));
        placemark.appendChild(extendedData);

        // 多几何图形（三层区域）
        Element multiGeometry = doc.createElement("MultiGeometry");
        multiGeometry.appendChild(createCirclePolygon(doc, gf, "核心区", gf.getCoreRadius(), "ff0000ff")); // 红色
        multiGeometry.appendChild(createCirclePolygon(doc, gf, "缓冲区", gf.getCoreRadius() + gf.getBufferRadius(), "ffff00ff")); // 黄色
        multiGeometry.appendChild(createCirclePolygon(doc, gf, "告警区", gf.getCoreRadius() + gf.getBufferRadius() + gf.getAlertRadius(), "ff00ff00")); // 绿色
        placemark.appendChild(multiGeometry);

        return placemark;
    }

    // 生成圆形多边形及其样式
    private static Element createCirclePolygon(Document doc, Geofence gf, String desc, double radius, String color) {
        Element polygon = doc.createElement("Polygon");

        // 描述
        Element descElem = doc.createElement("description");
        descElem.setTextContent(desc);
        polygon.appendChild(descElem);

        // 样式
        Element style = doc.createElement("Style");
        Element lineStyle = doc.createElement("LineStyle");
        Element colorElem = doc.createElement("color");
        colorElem.setTextContent(color);
        Element widthElem = doc.createElement("width");
        widthElem.setTextContent("2");
        lineStyle.appendChild(colorElem);
        lineStyle.appendChild(widthElem);
        style.appendChild(lineStyle);
        polygon.appendChild(style);

        // 坐标
        Element outerBoundary = doc.createElement("outerBoundaryIs");
        Element linearRing = doc.createElement("LinearRing");
        Element coordinates = doc.createElement("coordinates");
        coordinates.setTextContent(generateCircleCoordinates(gf.getCoreLongitude(), gf.getCoreLatitude(), radius));
        linearRing.appendChild(coordinates);
        outerBoundary.appendChild(linearRing);
        polygon.appendChild(outerBoundary);

        return polygon;
    }

    // 生成圆形坐标点（近似32边形）
    private static String generateCircleCoordinates(double lon, double lat, double radius) {
        StringBuilder sb = new StringBuilder();
        int points = 32;
        for (int i = 0; i <= points; i++) {
            double angle = 2 * Math.PI * i / points;
            double dx = (radius * Math.cos(angle)) / METERS_PER_DEGREE;
            double dy = (radius * Math.sin(angle)) / METERS_PER_DEGREE;
            sb.append(lon + dx).append(",").append(lat + dy).append(",0 ");
        }
        return sb.toString();
    }

    // 添加扩展数据字段
    private static void addData(Document doc, Element parent, String name, String value) {
        Element data = doc.createElement("Data");
        data.setAttribute("name", name);
        Element valueElem = doc.createElement("value");
        valueElem.setTextContent(value);
        data.appendChild(valueElem);
        parent.appendChild(data);
    }

    // 从KML解析禁飞区列表
    public static List<Geofence> parseFromKml(InputStream input) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(input);
            NodeList placemarks = doc.getElementsByTagName("Placemark");
            List<Geofence> geofences = new ArrayList<>();

            for (int i = 0; i < placemarks.getLength(); i++) {
                Element pm = (Element) placemarks.item(i);
                Geofence gf = parsePlacemark(pm);
                if (gf != null) geofences.add(gf);
            }
            return geofences;
        } catch (Exception e) {
            throw new KmlParseException("KML解析失败: " + e.getMessage());
        }
    }

    // 解析单个Placemark
    private static Geofence parsePlacemark(Element placemark) {
        try {
            // 解析名称
            String name = placemark.getElementsByTagName("name").item(0).getTextContent();

            // 解析扩展数据
            Element extData = (Element) placemark.getElementsByTagName("ExtendedData").item(0);
            double coreRadius = getRequiredDataValue(extData, "coreRadius");
            double bufferRadius = getRequiredDataValue(extData, "bufferRadius");
            double alertRadius = getRequiredDataValue(extData, "alertRadius");

            // 解析圆心（取第一个坐标点）
            Element polygon = (Element) placemark.getElementsByTagName("Polygon").item(0);
            String[] firstCoord = polygon.getElementsByTagName("coordinates").item(0)
                    .getTextContent().split(" ")[0].split(",");
            double lon = Double.parseDouble(firstCoord[0]);
            double lat = Double.parseDouble(firstCoord[1]);

            // 构建对象
            Geofence gf = new Geofence();
            gf.setName(name);
            gf.setCoreLongitude(lon);
            gf.setCoreLatitude(lat);
            gf.setCoreRadius(coreRadius);
            gf.setBufferRadius(bufferRadius);
            gf.setAlertRadius(alertRadius);
            return gf;
        } catch (Exception e) {
            throw new KmlParseException("解析Placemark失败: " + e.getMessage());
        }
    }

    // 获取必须的扩展数据字段
    private static double getRequiredDataValue(Element extData, String name) {
        NodeList dataList = extData.getElementsByTagName("Data");
        for (int i = 0; i < dataList.getLength(); i++) {
            Element data = (Element) dataList.item(i);
            if (data.getAttribute("name").equals(name)) {
                return Double.parseDouble(data.getElementsByTagName("value").item(0).getTextContent());
            }
        }
        throw new KmlParseException("缺失必要字段: " + name);
    }

    // 自定义KML解析异常
    public static class KmlParseException extends RuntimeException {
        public KmlParseException(String message) {
            super(message);
        }
    }
}
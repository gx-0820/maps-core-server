package com.example.coreserver.service.business;

import org.springframework.stereotype.Service;

@Service
public class AzimuthElevationCalculator {

    // 地球椭球参数（WGS84）
    private static final double A = 6378137.0;        // 赤道半径（m）
    private static final double B = 6356752.3142;    // 极半径（m）
    private static final double E2 = (A*A - B*B) / (A*A); // 第一偏心率平方
    private double Lat = Double.NaN;
    private double Lon = Double.NaN;
    private double Alt = Double.NaN;
    private NorthAngle northAngleservice;

    // 注入服务（构造函数）
    public AzimuthElevationCalculator(NorthAngle service) {
        this.northAngleservice = service;
    }

    // 判断是否已初始化
    private boolean isInitialized() {
        return !Double.isNaN(Lat) && !Double.isNaN(Lon) && !Double.isNaN(Alt);
    }

    /**
     * 计算从固定点到目标点的方位角和俯仰角
     *
//     * @param lat1 固定点纬度（度）
//     * @param lon1 固定点经度（度）
//     * @param h1   固定点高度（m）
     * @param lat2 目标点纬度（度）
     * @param lon2 目标点经度（度）
     * @param h2   目标点高度（m）
     * @return double[]{azimuth(度), elevation(度)}
     */
    public double[] calculateAzEl(double lat2, double lon2, double h2) {

        if (!isInitialized()){
            if (northAngleservice == null) {
                throw new IllegalStateException("NorthAngle service not set");
            }
            double[] res = northAngleservice.getLatLonAlt();
            this.Lat = res[0];
            this.Lon = res[1];
            this.Alt = res[2];
        }
        // 1. 转换为弧度
        double φ1 = Math.toRadians(this.Lat);
        double λ1 = Math.toRadians(this.Lon);
        double φ2 = Math.toRadians(lat2);
        double λ2 = Math.toRadians(lon2);

        // 2. 固定点转 ECEF
        double[] ecef1 = geodeticToECEF(φ1, λ1, this.Alt);

        // 3. 目标点转 ECEF
        double[] ecef2 = geodeticToECEF(φ2, λ2, h2);

        // 4. ECEF 差值向量
        double dx = ecef2[0] - ecef1[0];
        double dy = ecef2[1] - ecef1[1];
        double dz = ecef2[2] - ecef1[2];

        // 5. ECEF 转 ENU（站心坐标系：东、北、天顶）
        double[] enu = ecefToENU(dx, dy, dz, φ1, λ1);

        double east = enu[0];
        double north = enu[1];
        double up = enu[2];

        // 6. 计算方位角
        double azimuth = Math.atan2(east, north); // atan2(东, 北)
        azimuth = Math.toDegrees(azimuth);
        if (azimuth < 0) azimuth += 360.0;

        // 7. 计算俯仰角
        double slantRange = Math.sqrt(east*east + north*north + up*up);
        double elevation = Math.asin(up / slantRange);
        elevation = Math.toDegrees(elevation);

        return new double[]{azimuth, elevation};
    }


    /**
     * 计算两个经纬高点之间的 3D 距离（单位：米）
     * @param lat2 点2纬度（度）
     * @param lon2 点2经度（度）
     * @param h2   点2高度（米）
     * @return 3D 距离（米）
     */
    public double distance3D(double lat2, double lon2, double h2) {

        if (!isInitialized()){
            if (northAngleservice == null) {
                throw new IllegalStateException("NorthAngle service not set");
            }
            double[] res = northAngleservice.getLatLonAlt();
            this.Lat = res[0];
            this.Lon = res[1];
            this.Alt = res[2];
        }
        // 1. 转换为弧度
        double φ1 = Math.toRadians(this.Lat);
        double λ1 = Math.toRadians(this.Lon);
        double φ2 = Math.toRadians(lat2);
        double λ2 = Math.toRadians(lon2);

        double[] p1 = geodeticToECEF(φ1, λ1, this.Alt);
        double[] p2 = geodeticToECEF(φ2, λ2, h2);

        double dx = p2[0] - p1[0];
        double dy = p2[1] - p1[1];
        double dz = p2[2] - p1[2];

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * 地理坐标（纬度、经度、高度）转 ECEF（地心地固坐标）
     */
    private double[] geodeticToECEF(double φ, double λ, double h) {
        double sinφ = Math.sin(φ);
        double cosφ = Math.cos(φ);
        double sinλ = Math.sin(λ);
        double cosλ = Math.cos(λ);

        double N = A / Math.sqrt(1 - E2 * sinφ * sinφ); // 卯酉圈曲率半径

        double x = (N + h) * cosφ * cosλ;
        double y = (N + h) * cosφ * sinλ;
        double z = (N * (1 - E2) + h) * sinφ;

        return new double[]{x, y, z};
    }

    /**
     * ECEF 坐标差转 ENU（站心坐标系：东-北-天顶）
     * 注意：这里输入的是相对于参考点的 ECEF 向量 (dx, dy, dz)
     */
    private static double[] ecefToENU(double dx, double dy, double dz, double φ, double λ) {
        double sinφ = Math.sin(φ);
        double cosφ = Math.cos(φ);
        double sinλ = Math.sin(λ);
        double cosλ = Math.cos(λ);

        double east  = -sinλ * dx + cosλ * dy;
        double north = -sinφ * cosλ * dx - sinφ * sinλ * dy + cosφ * dz;
        double up    =  cosφ * cosλ * dx + cosφ * sinλ * dy + sinφ * dz;

        return new double[]{east, north, up};
    }



    // 测试示例
//    public static void main(String[] args) {
//        // 固定点：北京某地
//        double lat1 = 39.9042;  // 北纬
//        double lon1 = 116.4074; // 东经
//        double h1 = 50;         // 高度 50 米
//
//        // 目标点：上海某地
//        double lat2 = 31.2304;
//        double lon2 = 121.4737;
//        double h2 = 100;
//
//        double[] result = calculateAzEl(lat2, lon2, h2);
//        double azimuth = result[0];
//        double elevation = result[1];
//
//        System.out.printf("方位角: %.4f°\n", azimuth);
//        System.out.printf("俯仰角: %.4f°\n", elevation);
//    }
}


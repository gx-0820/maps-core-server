package com.example.coreserver.service.business;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class NorthAngle {

    private static AzimuthElevationCalculator a;
        public float getNorthAngle(){
            String filePath = "./northangle.txt";
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            InputStreamReader reader = new InputStreamReader(fin);
            BufferedReader buffReader = new BufferedReader(reader);
            String strTmp = "";
            float angle = 0;
            while(true){
                try {
                    if (!((strTmp = buffReader.readLine())!=null)) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                angle = Float.parseFloat(strTmp);
            }
            try {
                buffReader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return angle;
        }

        public static double[] getLatLonAlt(){
            String filePath = "./LatLonAlt.txt";
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            InputStreamReader reader = new InputStreamReader(fin);
            BufferedReader buffReader = new BufferedReader(reader);
            String strTmp = "";
            double[] res = new double[3];

            for (int i = 0 ; i<3;i++){
                try {
                    if (!((strTmp = buffReader.readLine())!=null)) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                res[i] = Double.parseDouble(strTmp);
            }
            try {
                buffReader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return res;
        }

        public static void main(String[] args){
            double[] test = getLatLonAlt();
            System.out.println(test[0]);
        }
}

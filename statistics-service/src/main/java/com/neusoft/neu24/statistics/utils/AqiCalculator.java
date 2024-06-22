package com.neusoft.neu24.statistics.utils;

import com.neusoft.neu24.entity.Aqi;
import org.springframework.stereotype.Component;

import java.util.List;

public class AqiCalculator {

    private final List<Aqi> aqiList;

    public AqiCalculator(List<Aqi> aqiList) {
        this.aqiList = aqiList;
    }

    // 根据污染物类型和浓度计算AQI值
    public int calculateAQI(String pollutant, double concentration) {
        double[] breakpoints = new double[aqiList.size() + 1];
        double[] aqiValues = new double[aqiList.size() + 1];

        for (int i = 0; i < aqiList.size(); i++) {
            Aqi aqi = aqiList.get(i);
            switch (pollutant) {
                case "spm":
                    breakpoints[i] = aqi.getSpmMin();
                    aqiValues[i] = aqi.getAqiValMin();
                    break;
                case "so2":
                    breakpoints[i] = aqi.getSo2Min();
                    aqiValues[i] = aqi.getAqiValMin();
                    break;
                case "co":
                    breakpoints[i] = aqi.getCoMin();
                    aqiValues[i] = aqi.getAqiValMin();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown pollutant: " + pollutant);
            }
        }

        switch (pollutant) {
            case "spm":
                breakpoints[aqiList.size()] = aqiList.get(aqiList.size() - 1).getSpmMax();
                break;
            case "so2":
                breakpoints[aqiList.size()] = aqiList.get(aqiList.size() - 1).getSo2Max();
                break;
            case "co":
                breakpoints[aqiList.size()] = aqiList.get(aqiList.size() - 1).getCoMax();
                break;
            default:
                throw new IllegalArgumentException("Unknown pollutant: " + pollutant);
        }
        aqiValues[aqiList.size()] = aqiList.get(aqiList.size() - 1).getAqiValMax();

        return calculateAQI(concentration, breakpoints, aqiValues);
    }

    // 根据浓度值和分段点和对应AQI值计算AQI值
    private int calculateAQI(double concentration, double[] breakpoints, double[] aqiValues) {
        double result = 0;
        for (int i = 0; i < breakpoints.length - 1; i++) {
            if (concentration >= breakpoints[i] && concentration <= breakpoints[i + 1]) {
                result = ((aqiValues[i + 1] - aqiValues[i]) / (breakpoints[i + 1] - breakpoints[i]))
                        * (concentration - breakpoints[i]) + aqiValues[i];
                break;
            }
        }
        return (int) Math.ceil(result);
    }

    // 获取健康关注程度
    public String getHealthConcern(int aqi) {
        for (Aqi a : aqiList) {
            if (aqi >= a.getAqiValMin() && aqi <= a.getAqiValMax()) {
                return a.getAqiExplain();
            }
        }
        return "AQI值无效";
    }
}

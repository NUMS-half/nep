package com.neusoft.neu24.aqi.utils;

import com.neusoft.neu24.entity.Aqi;
import com.neusoft.neu24.entity.Statistics;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <b>AQI计算器</b>
 *
 * @since 2024-05-21
 * @author Team-NEU-NanHu
 */
public class AqiCalculator {

    /**
     * AQI列表
     */
    private final List<Aqi> aqiList;

    /**
     * AQI映射
     */
    private final Map<Integer, Aqi> aqiMap;

    /**
     * 构造方法
     *
     * @param aqiList AQI列表
     */
    public AqiCalculator(List<Aqi> aqiList) {
        this.aqiList = aqiList;
        this.aqiMap = aqiList.stream().collect(Collectors.toMap(Aqi::getAqiId, a -> a));
    }

    /**
     * 验证传入的实测值是否合法
     * @param statistics 检测信息
     * @return 是否合法
     */
    public boolean validateAqi(Statistics statistics) {
        Aqi aqi = this.aqiMap.get(statistics.getAqiId());
        return aqi != null && aqi.getAqiId().equals(getMaxFormThree(
                statistics.getSo2Level(),
                statistics.getCoLevel(),
                statistics.getSpmLevel()));
    }

    /**
     * 验证传入的实测值与对应的等级是否匹配
     * @param level 等级
     * @param value 实测值
     * @return 是否匹配
     */
    public boolean validateItem(int level, int value, String pollutant) {
        Aqi aqi = this.aqiMap.get(level);
        return switch ( pollutant ) {
            case "spm" -> aqi != null && isInRange(value, aqi.getSpmMin(), aqi.getSpmMax());
            case "co" -> aqi != null && isInRange(value, aqi.getCoMin(), aqi.getCoMax());
            case "so2" -> aqi != null && isInRange(value, aqi.getSo2Min(), aqi.getSo2Max());
            default -> throw new IllegalArgumentException("未知的污染物类型: " + pollutant);
        };
    }

    /**
     * 辅助方法：判断值是否在给定的范围内
     *
     * @param value 实际值
     * @param min   最小值
     * @param max   最大值
     * @return 是否在范围内
     */
    private static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    private static int getMaxFormThree(int a, int b, int c) {
        return Math.max(Math.max(a, b), c);
    }

    /**
     * 根据污染物类型和浓度计算AQI值
     * @param pollutant 污染物类型
     * @param concentration 浓度
     * @return AQI值
     */
    public int calculateAQI(String pollutant, double concentration) {
        double[] breakpoints = new double[aqiList.size() + 1];
        double[] aqiValues = new double[aqiList.size() + 1];

        for ( int i = 0; i < aqiList.size(); i++ ) {
            Aqi aqi = aqiList.get(i);
            switch ( pollutant ) {
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

        switch ( pollutant ) {
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

    /**
     * 根据浓度值和分段点和对应AQI值计算AQI值
     * @param concentration 浓度值
     * @param breakpoints 分段点
     * @param aqiValues 对应AQI值
     * @return AQI值
     */
    private int calculateAQI(double concentration, double[] breakpoints, double[] aqiValues) {
        double result = 0;
        for ( int i = 0; i < breakpoints.length - 1; i++ ) {
            if ( concentration >= breakpoints[i] && concentration <= breakpoints[i + 1] ) {
                result = ((aqiValues[i + 1] - aqiValues[i]) / (breakpoints[i + 1] - breakpoints[i]))
                        * (concentration - breakpoints[i]) + aqiValues[i];
                break;
            }
        }
        return (int) Math.ceil(result);
    }

    /**
     * 获取健康关注程度
     * @param aqi AQI值
     * @return 健康关注程度
     */
    public String getHealthConcern(int aqi) {
        for ( Aqi a : aqiList ) {
            if ( aqi >= a.getAqiValMin() && aqi <= a.getAqiValMax() ) {
                return a.getAqiExplain();
            }
        }
        return "AQI值无效";
    }
}

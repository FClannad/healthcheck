package com.example.utils;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 文本相似度计算工具类
 * 用于文献去重和相似度检测
 */
@Component
public class SimilarityUtil {

    /**
     * 计算两个字符串的Jaccard相似度
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 相似度值 (0-1之间)
     */
    public double calculateJaccardSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0.0;
        }
        
        if (str1.equals(str2)) {
            return 1.0;
        }
        
        Set<String> set1 = getWordSet(str1.toLowerCase());
        Set<String> set2 = getWordSet(str2.toLowerCase());
        
        if (set1.isEmpty() && set2.isEmpty()) {
            return 1.0;
        }
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return (double) intersection.size() / union.size();
    }

    /**
     * 计算编辑距离相似度
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 相似度值 (0-1之间)
     */
    public double calculateEditDistanceSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0.0;
        }
        
        if (str1.equals(str2)) {
            return 1.0;
        }
        
        int editDistance = calculateEditDistance(str1.toLowerCase(), str2.toLowerCase());
        int maxLength = Math.max(str1.length(), str2.length());
        
        if (maxLength == 0) {
            return 1.0;
        }
        
        return 1.0 - (double) editDistance / maxLength;
    }

    /**
     * 综合相似度计算（结合Jaccard和编辑距离）
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 相似度值 (0-1之间)
     */
    public double calculateCombinedSimilarity(String str1, String str2) {
        double jaccardSim = calculateJaccardSimilarity(str1, str2);
        double editSim = calculateEditDistanceSimilarity(str1, str2);
        
        // 加权平均：Jaccard权重0.6，编辑距离权重0.4
        return jaccardSim * 0.6 + editSim * 0.4;
    }

    /**
     * 判断两个标题是否相似（用于去重）
     * @param title1 标题1
     * @param title2 标题2
     * @param threshold 相似度阈值 (默认0.8)
     * @return 是否相似
     */
    public boolean isSimilar(String title1, String title2, double threshold) {
        return calculateCombinedSimilarity(title1, title2) >= threshold;
    }

    /**
     * 使用默认阈值0.8判断相似性
     */
    public boolean isSimilar(String title1, String title2) {
        return isSimilar(title1, title2, 0.8);
    }

    /**
     * 将字符串分割为词集合
     */
    private Set<String> getWordSet(String str) {
        Set<String> words = new HashSet<>();
        
        // 移除标点符号并分割单词
        String cleanStr = str.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5\\s]", " ");
        String[] wordArray = cleanStr.split("\\s+");
        
        for (String word : wordArray) {
            if (!word.trim().isEmpty() && word.length() > 1) {
                words.add(word.trim());
            }
        }
        
        return words;
    }

    /**
     * 计算编辑距离（Levenshtein距离）
     */
    private int calculateEditDistance(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();
        
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        // 初始化
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        
        // 动态规划计算编辑距离
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + 1
                    );
                }
            }
        }
        
        return dp[len1][len2];
    }

    /**
     * 快速相似度检测（用于大量数据的初步筛选）
     * 基于长度和首尾字符的快速判断
     */
    public boolean quickSimilarityCheck(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return false;
        }
        
        if (str1.equals(str2)) {
            return true;
        }
        
        // 长度差异过大，直接判断为不相似
        int lengthDiff = Math.abs(str1.length() - str2.length());
        if (lengthDiff > Math.max(str1.length(), str2.length()) * 0.3) {
            return false;
        }
        
        // 检查前缀和后缀相似性
        String prefix1 = str1.length() > 10 ? str1.substring(0, 10).toLowerCase() : str1.toLowerCase();
        String prefix2 = str2.length() > 10 ? str2.substring(0, 10).toLowerCase() : str2.toLowerCase();
        
        return calculateJaccardSimilarity(prefix1, prefix2) > 0.5;
    }
}

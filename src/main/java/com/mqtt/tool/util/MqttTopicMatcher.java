package com.mqtt.tool.util;

/** MqttTopicMatcher
 * @author chics
 * @date 2026/1/5
 */
public class MqttTopicMatcher {

    /**
     * 检查主题是否匹配给定的模式
     * @param pattern 主题模式，如 "testtopic/+#", "testtopic/+/abc/+/ds"
     * @param topic 实际主题，如 "testtopic/abc"
     * @return 如果匹配返回 true，否则返回 false
     */
    public static boolean match(String pattern, String topic) {
        if (pattern == null || topic == null) {
            return false;
        }
        // 将模式和主题按 '/' 分割成数组
        String[] patternParts = pattern.split("/");
        String[] topicParts = topic.split("/");
        int pIdx = 0; // 模式索引
        int tIdx = 0; // 主题索引
        while (pIdx < patternParts.length && tIdx < topicParts.length) {
            String p = patternParts[pIdx];
            // 如果模式部分是 '#'，表示匹配剩余的所有主题部分
            if ("#".equals(p)) {
                // '#' 必须是模式的最后一个部分
                return pIdx == patternParts.length - 1;
            }
            // 如果模式部分是 '+'，表示匹配任意单个主题部分
            else if ("+".equals(p)) {
                pIdx++;
                tIdx++;
            }
            // 普通字符串匹配
            else {
                if (!p.equals(topicParts[tIdx])) {
                    return false;
                }
                pIdx++;
                tIdx++;
            }
        }
        // 如果模式已经遍历完，但主题还有剩余部分，检查模式最后是否是 '#'
        if (pIdx < patternParts.length) {
            return patternParts[pIdx].equals("#") && pIdx == patternParts.length - 1;
        }
        // 如果主题已遍历完但模式还有剩余，说明不匹配（除非剩余部分是 '#'）
        if (tIdx < topicParts.length) {
            return false;
        }
        return true;
    }

    /**
     * 批量匹配主题和模式
     * @param patterns 多个主题模式
     * @param topic 要匹配的主题
     * @return 匹配结果列表
     */
    public static java.util.List<Boolean> matchMultiple(String[] patterns, String topic) {
        java.util.List<Boolean> results = new java.util.ArrayList<>();
        for (String pattern : patterns) {
            results.add(match(pattern, topic));
        }
        return results;
    }
}

package com.bu.dong.fuseki.poker;

import java.util.*;

// 主计算引擎
public class TexasHoldemSimulator {
    private List<Card> deck;
    private final List<Player> players;
    private final List<Card> communityCards;
    private final int simulations = 1000000;  // 默认10万次模拟

    // 扑克牌实体类（支持花色与点数解析）
    private static class Card {
        String rank;
        String suit;

        public Card(String code) {  // 构造函数（如"Ah"表示红心A）
            this.rank = code.substring(0, code.length() - 1);
            this.suit = code.substring(code.length() - 1);
        }
    }

    // 玩家实体类（含手牌与胜率统计）
    private static class Player {
        int id;
        Card[] hand = new Card[2];
        int wins = 0;

        public Player(int id, Card card1, Card card2) {
            this.id = id;
            hand[0] = card1;
            hand[1] = card2;
        }
    }

    public TexasHoldemSimulator(List<Player> players, List<Card> communityCards) {
        initializeDeck(players, communityCards);
        this.players = players;
        this.communityCards = communityCards;
    }

    // 初始化剩余牌池（排除已知牌）
    private void initializeDeck(List<Player> players, List<Card> communityCards) {
        // 创建完整牌库（52张）
        deck = new ArrayList<>();
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        String[] suits = {"s", "h", "d", "c"};
        for (String s : suits) {
            for (String r : ranks) {
                deck.add(new Card(r + s));
            }
        }
        // 移除已发牌
        players.forEach(p -> deck.removeAll(Arrays.asList(p.hand)));
        deck.removeAll(communityCards);
    }

    // 蒙特卡洛模拟核心逻辑
    public void runSimulations() {
        for (int i = 0; i < simulations; i++) {
            List<Card> remainingDeck = new ArrayList<>(deck);
            Collections.shuffle(remainingDeck);

            // 生成剩余2张公共牌
            List<Card> simulatedCommunity = new ArrayList<>(communityCards);
            simulatedCommunity.add(remainingDeck.remove(0));
            simulatedCommunity.add(remainingDeck.remove(0));

            // 评估所有玩家牌型并比较
            evaluateRound(simulatedCommunity);
        }
    }

    // 胜负判断与统计
    private void evaluateRound(List<Card> fullCommunity) {
        Map<Player, Integer> scores = new HashMap<>();
        for (Player p : players) {
            List<Card> allCards = new ArrayList<>(Arrays.asList(p.hand));
            allCards.addAll(fullCommunity);
            scores.put(p, evaluateHand(allCards));
        }

        int maxScore = Collections.max(scores.values());
        long winners = scores.values().stream().filter(v -> v == maxScore).count();

        for (Player p : players) {
            if (scores.get(p) == maxScore) {
                p.wins += (winners > 1) ? 0 : 1;  // 处理平局
            }
        }
    }

    // 牌型评估算法（简化版）
    // 牌型权重表（数值越大，牌型越强）
    private static final int HIGH_CARD = 1;
    private static final int PAIR = 2;
    private static final int TWO_PAIR = 3;
    private static final int THREE_OF_A_KIND = 4;
    private static final int STRAIGHT = 5;
    private static final int FLUSH = 6;
    private static final int FULL_HOUSE = 7;
    private static final int FOUR_OF_A_KIND = 8;
    private static final int STRAIGHT_FLUSH = 9;

    private int evaluateHand(List<Card> cards) {
        // 1. 转换为数值特征
        int[] ranks = new int[13]; // 各点数出现次数（A-K对应0-12）
        int[] suits = new int[4];  // 各花色出现次数（s,h,d,c对应0-3）
        int[] rankValues = new int[cards.size()]; // 数值化点数（A=14, K=13...2=2）

        for (int i = 0; i < cards.size(); i++) {
            String r = cards.get(i).rank;
            int val = switch (r) {
                case "A" -> 14;
                case "K" -> 13;
                case "Q" -> 12;
                case "J" -> 11;
                case "10" -> 10;
                default -> Integer.parseInt(r); // 2-9
            };
            rankValues[i] = val;
            ranks[val - 2]++; // 点数计数（A=14对应索引12）
            suits["shdc".indexOf(cards.get(i).suit)]++;
        }
        Arrays.sort(rankValues); // 排序便于顺子判断

        // 2. 关键牌型判断
        boolean isFlush = Arrays.stream(suits).anyMatch(count -> count >= 5);
        boolean isStraight = checkStraight(rankValues);
        int pairs = (int) Arrays.stream(ranks).filter(c -> c == 2).count();
        boolean hasThree = Arrays.stream(ranks).anyMatch(c -> c >= 3);
        boolean hasFour = Arrays.stream(ranks).anyMatch(c -> c == 4);

        // 3. 确定最大牌型权重
        if (isFlush && isStraight) {
            return (rankValues[4] == 14) ? STRAIGHT_FLUSH + 1 : STRAIGHT_FLUSH; // 皇家同花顺特殊处理
        } else if (hasFour) {
            return FOUR_OF_A_KIND;
        } else if (hasThree && pairs >= 1) {
            return FULL_HOUSE;
        } else if (isFlush) {
            return FLUSH;
        } else if (isStraight) {
            return STRAIGHT;
        } else if (hasThree) {
            return THREE_OF_A_KIND;
        } else if (pairs >= 2) {
            return TWO_PAIR;
        } else if (pairs == 1) {
            return PAIR;
        } else {
            return HIGH_CARD;
        }
    }

    // 辅助方法：判断顺子（处理A-2-3-4-5的特殊情况）
    private boolean checkStraight(int[] sortedRanks) {
        // 标准顺子检测
        for (int i = 0; i <= sortedRanks.length - 5; i++) {
            if (sortedRanks[i + 4] - sortedRanks[i] == 4) return true;
        }
        // 检测A-2-3-4-5
        return sortedRanks[0] == 2 && sortedRanks[1] == 3 && sortedRanks[2] == 4 &&
                sortedRanks[3] == 5 && sortedRanks[sortedRanks.length - 1] == 14;
    }

    public static void main(String[] args) {
        // 示例输入：3个玩家手牌+3张公共牌
        Player p1 = new Player(1, new Card("Ks"), new Card("Kd"));  // 玩家1手牌
        Player p2 = new Player(2, new Card("Ah"), new Card("Qh"));  // 玩家2手牌
        Player p3 = new Player(3, new Card("Js"), new Card("3s"));  // 玩家3手牌
        List<Card> community = Collections.emptyList();

        TexasHoldemSimulator simulator = new TexasHoldemSimulator(
                Arrays.asList(p1, p2, p3), community
        );
        simulator.runSimulations();

        // 输出结果
        for (Player p : simulator.players) {
            double rate = (double) p.wins / simulator.simulations * 100;
            System.out.printf("玩家%d胜率: %.2f%%\n", p.id, rate);
        }
    }
}
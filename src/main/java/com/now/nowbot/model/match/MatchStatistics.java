package com.now.nowbot.model.match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchStatistics {
    Map<Integer, UserMatchData> users;
    List<GameRound> gameRounds = new ArrayList<>();
    Map<String, Integer> teamPoint = new HashMap<>();

    Integer scoreNum;
    Double totalAMG = 0d;
    Double minMQ = 100d;


    public void calculate() {
        //挨个用户计算AMG,并记录总AMG
        for (var user : users.values()) {
            user.calculateAMG();
            totalAMG += user.getAMG();
        }

        //挨个计算MQ,并记录最小的MQ
        for (var user : users.values()) {
            user.calculateMQ(totalAMG / users.size());
            if (user.getMQ() < minMQ)
                minMQ = user.getMQ();
        }

        //挨个计算MRA和MDRA
        for (var user : users.values()) {
            user.calculateMRA(minMQ);
            user.calculateMDRA(users.size(), scoreNum);
        }

        //如果人数<=2或者没换过人, 则MRA==MQ
        if (users.size() <= 2 || users.size() * gameRounds.size() == scoreNum) {
            for (var user : users.values()) {
                user.setMRA(user.getMQ());
            }
        }

        //计算比分
        for (var round : gameRounds) {
            String team = round.getWinningTeam();
            teamPoint.put(team, teamPoint.getOrDefault(team, 0) + 1);
            for (Integer id : round.getUserScores().keySet()) {
                var user = users.get(id);
                if (user.getTeam().equals(team)) {
                    user.setWins(user.getWins() + 1);
                } else user.setLost(user.getLost() + 1);
            }
        }

    }

    public Map<String, Integer> getTeamPoint() {
        return teamPoint;
    }

    public void setTeamPoint(Map<String, Integer> teamPoint) {
        this.teamPoint = teamPoint;
    }

    public Integer getScoreNum() {
        return scoreNum;
    }

    public void setScoreNum(Integer scoreNum) {
        this.scoreNum = scoreNum;
    }

    public Double getTotalAMG() {
        return totalAMG;
    }

    public void setTotalAMG(Double totalAMG) {
        this.totalAMG = totalAMG;
    }

    public Double getMinMQ() {
        return minMQ;
    }

    public void setMinMQ(Double minMQ) {
        this.minMQ = minMQ;
    }

    public Map<Integer, UserMatchData> getUsers() {
        return users;
    }

    public void setUsers(Map<Integer, UserMatchData> users) {
        this.users = users;
    }

    public List<GameRound> getGameRounds() {
        return gameRounds;
    }

    public void setGameRounds(List<GameRound> gameRounds) {
        this.gameRounds = gameRounds;
    }
}

package uk.co.createanet.footballformapp.models;

/**
 * Created by matt on 03/07/2014.
 */
public class Fixture {

    public int id;
    public int teams_home_id;
    public String teams_home_name;
    public int team_home_score;
    public int team_home_standing;
    public int teams_away_id;
    public String teams_away_name;
    public int team_away_score;
    public int team_away_standing;
    public int league_id;

    public static enum GAME_STATUS {
        WON, DREW, LOST
    }

    public GAME_STATUS getGameStatus(int teamId){
        if(team_home_score == team_away_score){
            return GAME_STATUS.DREW;
        }

        return (teams_home_id == teamId) ?
                (team_home_score > team_away_score ? GAME_STATUS.WON : GAME_STATUS.LOST) :
                (team_home_score < team_away_score ? GAME_STATUS.WON : GAME_STATUS.LOST);
    }


}

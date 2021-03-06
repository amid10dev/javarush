package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {

    private PlayerRepository playerRepository;

    public PlayerServiceImpl() {

    }

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        super();
        this.playerRepository = playerRepository;
    }


    @Override
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    @Override
    public Player getPlayer(Long id) {
        return playerRepository.findById(id).orElse(null);

    }

    @Override
    public void deletePlayer(Player player) {
        playerRepository.delete(player);
    }

    @Override
    public Player updatePlayer(Player oldPlayer, Player newPlayer) throws IllegalAccessException {
        boolean shouldChangeRating = false;

        final String name = newPlayer.getName();
        if (name != null) {
            if (isNameValid(name)) {
                oldPlayer.setName(name);
            } else {
                throw new IllegalArgumentException();
            }
        }
        final String title = newPlayer.getTitle();
        if (title != null) {
            if (isTitleValid(title)) {
                oldPlayer.setTitle(title);
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (newPlayer.getRace() != null) {
            oldPlayer.setRace(newPlayer.getRace());
        }

        if (newPlayer.getProfession() != null) {
            oldPlayer.setProfession(newPlayer.getProfession());
        }
        final Integer experience = newPlayer.getExperience();
        if (experience != null) {
            if (isExperienceValid(experience)) {
                oldPlayer.setExperience(experience);
            } else {
                throw new IllegalArgumentException();
            }
        }

        final Date birthDay = newPlayer.getBirthday();
        if (birthDay != null) {
            if (isBirthdayDateValid(birthDay)) {
                oldPlayer.setBirthday(birthDay);
                shouldChangeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (newPlayer.getBanned() != null) {
            oldPlayer.setBanned(newPlayer.getBanned());
            shouldChangeRating = true;
        }


      //  final int level = levelRating(oldPlayer.getExperience());
        oldPlayer.setLevel(levelRating(oldPlayer.getExperience()));


      //  final int nextLevel = experienceNextLevel(oldPlayer.getLevel(), oldPlayer.getExperience());
        oldPlayer.setUnitNextLevel(experienceNextLevel(oldPlayer.getLevel(), oldPlayer.getExperience()));


        playerRepository.save(oldPlayer);
        return oldPlayer;
    }


    @Override
    public List<Player> getPlayers(String name,
                                   String title,
                                   Race race,
                                   Profession profession,
                                   Long after,
                                   Long before,
                                   Boolean banned,
                                   Integer minExperience,
                                   Integer maxExperience,
                                   Integer minLevel,
                                   Integer maxLevel
                               //    Integer minUntilNextLevel,
                                 //  Integer maxUntilNextLevel
    ) {
        final Date afterDate = after == null ? null : new Date(after);
        final Date beforeDate = before == null ? null : new Date(before);
        final List<Player> list = new ArrayList<>();
        playerRepository.findAll().forEach((player) -> {
            if (name != null && !player.getName().contains(name)) return;
            if (title != null && !player.getTitle().contains(title)) return;
            if (race != null && player.getRace() != race) return;
            if (profession != null && player.getProfession() != profession) return;
            if (after != null && player.getBirthday().before(afterDate)) return;
            if (before != null && player.getBirthday().after(beforeDate)) return;
            if (banned != null && player.getBanned().booleanValue() != banned.booleanValue()) return;
            if (minExperience != null && player.getExperience().compareTo(minExperience) < 0) return;
            if (maxExperience != null && player.getExperience().compareTo(maxExperience) > 0) return;
            if (minLevel != null && player.getLevel().compareTo(minLevel) < 0) return;
            if (maxLevel != null && player.getLevel().compareTo(maxLevel) > 0) return;
        //    if (minUntilNextLevel != null && player.getUnitNextLevel().compareTo(minUntilNextLevel) < 0) return;
        //    if (maxUntilNextLevel != null && player.getUnitNextLevel().compareTo(maxUntilNextLevel) > 0) return;

            list.add(player);
        });


        return list;
    }

    @Override
    public List<Player> sortPlayer(List<Player> players, PlayerOrder order) {

        if (order != null) {
            players.sort((player1, player2) -> {
                switch (order) {
                    case ID:
                        return player1.getId().compareTo(player2.getId());
                    case NAME:
                        return player1.getName().compareTo(player2.getName());
                    case EXPERIENCE:
                        return player1.getExperience().compareTo(player2.getExperience());
                    case BIRTHDAY:
                        return player1.getBirthday().compareTo(player2.getBirthday());
                    case LEVEL:
                        return player1.getLevel().compareTo(player2.getLevel());
                    default:
                        return 0;
                }
            });
        }
        return players;
    }

    @Override
    public List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize) {
        final Integer page = pageNumber == null ? 0 : pageNumber;
        final Integer size = pageSize == null ? 3 : pageSize;
        final int from = page * size;
        int to = from + size;
        if (to > players.size()) to = players.size();

        return players.subList(from, to);
    }

    @Override
    public boolean isPlayerValid(Player player) {
        return player != null && isNameValid(player.getName())
                && isTitleValid(player.getTitle())
                && isExperienceValid(player.getExperience())
                && isBirthdayDateValid(player.getBirthday());
    }

    private boolean isNameValid(String value) {
        final int maxStringLength = 12;
        return value != null && !value.isEmpty() && value.length() <= maxStringLength;
    }

    private boolean isTitleValid(String value) {
        final int maxStringLength = 30;
        return value != null && !value.isEmpty() && value.length() <= maxStringLength;
    }

    private boolean isExperienceValid(Integer experience) {
        final int minExperience = 0;
        final int maxCrewSize = 10000000;
        return experience != null && experience.compareTo(minExperience) >= 0 && experience.compareTo(maxCrewSize) <= 0;
    }


    private boolean isBirthdayDateValid(Date prodDate) {
        final Date startProd = getDateForYear(2000);
        final Date endProd = getDateForYear(3000);
        return prodDate != null && prodDate.after(startProd) && prodDate.before(endProd);
    }

    private Date getDateForYear(int year) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    private int getYearFromDate(Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    @Override
    public int levelRating(int experience) {
        double L = (Math.sqrt(2500 + 200 * experience) - 50) / 100;
        return (int) L;
    }

    @Override
    public int experienceNextLevel(int levelRating, int experience) {
        return 50 * (levelRating + 1) * (levelRating + 2) - experience;

    }


}

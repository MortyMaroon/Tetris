package com.codenjoy.dojo.bot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Класс для хранения информации о фигур при её постановке
 */
@Getter
@AllArgsConstructor
@ToString
public class State {
    private Position position;
    private double points;
    private int place;
}

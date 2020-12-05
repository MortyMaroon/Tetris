package com.codenjoy.dojo.bot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class State {
    private Position position;
    private int point;
    private int place;
}

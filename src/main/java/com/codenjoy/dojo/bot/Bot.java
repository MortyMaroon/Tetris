package com.codenjoy.dojo.bot;

import com.codenjoy.dojo.services.Command;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.Rotation;
import com.codenjoy.dojo.tetris.client.Board;
import com.codenjoy.dojo.tetris.client.GlassBoard;
import com.codenjoy.dojo.tetris.model.Elements;

import java.util.*;

public class Bot {
    private final int SIZE = 17;
    private GlassBoard glassBoard;
    private List<Point> listBusyPoints;
    private Elements currentElement;
    private Point anchorPoint;
    private Point[] coordinatesPointsCurrentFigure;
    private int figureWidth;
    private ArrayList<State> listOfStagingOptions;
    private Position position = Position.UP;
    private int numberPossiblePosition = 0;

    /**
     * Инициализирует поля для работы основных методов
     * @param board
     */
    private void initialize(Board board) {
        this.glassBoard = board.getGlass();
        this.listBusyPoints = board.getGlass().getFigures();
        this.currentElement = board.getCurrentFigureType();
        this.anchorPoint = board.getCurrentFigurePoint();
        this.coordinatesPointsCurrentFigure = board.predictCurrentFigurePoints();
        this.figureWidth = getRightPointOfFigure().getX() - getLeftPointOfFigure().getX();
        this.listOfStagingOptions = new ArrayList();
        listBusyPoints.removeAll(Arrays.asList(coordinatesPointsCurrentFigure));
    }

    /**
     * Основной метод вызывающий все остальные
     * Определяет оптимальный ход на основании игрового поля
     * @param board Игровое поле
     * @return Список команд для перемещения фигуры в оптимальную позицию
     */
    public List<Command> move(Board board) {
        List<Command> result = null;
        initialize(board);
        numberPossiblePosition = SIZE - figureWidth;
        System.out.println(board.getGlass().toString());
        System.out.println("Тип фигуры: " + currentElement);
        switch (currentElement) {
//            case YELLOW:
//
//                checkingOptions(numberPossiblePosition);
//                listPoints.sort(Comparator.comparingInt(State::getPoint));
//                System.out.println(listPoints);
//                result = createMove(chooseBestMove());
//                break;

            case BLUE:
            case RED:
            case GREEN:

                for (int external = 0; external < 2; external++) {
                    checkingOptions(numberPossiblePosition);
                    turnFigure(board,external);
                }
                listOfStagingOptions.sort(Comparator.comparingInt(State::getPoints));
                System.out.println(listOfStagingOptions);
                result = createMove(chooseBestMove());
                break;

            case ORANGE:
            case CYAN:
            case PURPLE:
                for (int external = 0; external < 3; external++) {
                    checkingOptions(numberPossiblePosition);
                    turnFigure(board,external);
                }
                listOfStagingOptions.sort(Comparator.comparingInt(State::getPoints));
                System.out.println(listOfStagingOptions);
                result = createMove(chooseBestMove());
                break;
        }
        return result;
    }

    /**
     * Выбирает лучшую позицию из всех возможных
     * @return
     */
    private State chooseBestMove() {
        return listOfStagingOptions.get(0);
    }


    /**
     * Проходит все возможные положения текущей фигуры с подсчетом внутренних балов для каждого положения
     * @param numberPossiblePosition Количество возможных положений текущей фигуры
     */
    private void checkingOptions(int numberPossiblePosition) {
        show();
        System.out.println("Ширина фигуры: " + (figureWidth + 1));
        System.out.println("Количество вариантов постановки фигуры: " + numberPossiblePosition);
        moveFigureToLeftBorder();
        for (int internal = 0; internal <= numberPossiblePosition; internal++) {
            while (getBottomPointOfFigure().getY() > 0 && checkPosition()) {
                moveFigureBottom();
            }
            listOfStagingOptions.add(new State(position, countingPoints(), internal));
            moveFigureRight();
            show();
        }
    }


    /**
     * Изменяет координаты точек фигуры в зависимости от переданного параметра ващения
     * корректирует данные ширины фигуры и количества возможных постановок
     * @param board Объект для применения вращения
     * @param direction Ключ вращения фигуры
     */
    private void turnFigure(Board board, int direction) {
        switch (direction) {
            case 0:
                position = Position.RIGHT;
                coordinatesPointsCurrentFigure = board.predictCurrentFigurePoints(Rotation.CLOCKWIZE_90);
                break;
            case 1:
                position = Position.DOWN;
                coordinatesPointsCurrentFigure = board.predictCurrentFigurePoints(Rotation.CLOCKWIZE_180);
                break;
            case 2:
                position = Position.LEFT;
                coordinatesPointsCurrentFigure = board.predictCurrentFigurePoints(Rotation.CLOCKWIZE_270);
                break;
        }
        figureWidth = getRightPointOfFigure().getX() - getLeftPointOfFigure().getX();
        numberPossiblePosition = SIZE - figureWidth;
    }

    /**
     * Создает набор команд перемещения и вращения фигуры для постановки в переданую позицию
     * @param state Оптимальная позиция
     * @return Список команд
     */
    private List<Command> createMove(State state) {
        List<Command> result = new ArrayList<>();
        Position position = state.getPosition();
        int place = state.getPlace();
        int anchorPlace = anchorPoint.getX();
        switch (position) {
            case RIGHT: result.add(Command.ROTATE_CLOCKWISE_90); break;
            case DOWN: result.add(Command.ROTATE_CLOCKWISE_180); break;
            case LEFT: result.add(Command.ROTATE_CLOCKWISE_270); break;
        }
        if (place < anchorPlace) {
            for (int i = 0; i < anchorPlace - place; i++) {
                result.add(Command.LEFT);
            }
        } else {
            for (int i = 0; i < place - anchorPlace; i++) {
                result.add(Command.RIGHT);
            }
        }
        result.add(Command.DOWN);
        return result;
    }

    /**
     * Подсчитывает внутренние баллы текущей позиции
     * @return
     */
    private int countingPoints() {
        int numberEmptyPoint = countingEmptyPoint(getLeftPointOfFigure().getX(), getTopPointOfFigure().getY());
        System.out.println("Количество пустых ячеек: " + numberEmptyPoint);
        int towerHeight = getTopPointOfFigure().getY();
        return numberEmptyPoint + towerHeight;
    }

    /**
     * Считает количтво образовавшихся пустых точек под местом постановки фигуры
     * @param leftEdge Левая граница фигуры
     * @param topEdge Верхняя граница фигуры
     * @return Число пустых точек
     */
    private int countingEmptyPoint(int leftEdge, int topEdge) {
        int numberEmptyPoint = 0;
        for (int row = leftEdge; row < leftEdge + figureWidth; row++) {
            for (int column = topEdge; column >= 0; column--) {
                if (!checkYourPoint(row,column) && glassBoard.isFree(row,column)) {
                    numberEmptyPoint++;
                }
            }
        }
        return numberEmptyPoint;
    }

    /**
     * Проверяет является ли точка частьтю фигуры
     * @param x координата по оси x
     * @param y координата по оси y
     * @return true если точка является частью фигуры
     */
    private boolean checkYourPoint(int x, int y) {
        for (Point point: coordinatesPointsCurrentFigure) {
            if (point.itsMe(x, y)) return true;
        }
        return false;
    }

    /**
     * Проверяет каждую точку фигуры на вхождение в список координает занятых точек
     * @return true если нет вхождений
     */
    private boolean checkPosition() {
        for (Point point : coordinatesPointsCurrentFigure) {
            if (listBusyPoints.contains(point.shiftBottom(1))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Перемещает фигуру на 1 еденицу вниз
     */
    private void moveFigureBottom(){
        for (int i = 0; i < coordinatesPointsCurrentFigure.length; i++) {
            coordinatesPointsCurrentFigure[i] = coordinatesPointsCurrentFigure[i].shiftBottom(1);
        }
    }

    /**
     * Перемещает фигуру на 1 еденицу вправо
     */
    private void moveFigureRight() {
        for (int i = 0; i < coordinatesPointsCurrentFigure.length; i++) {
            coordinatesPointsCurrentFigure[i] = coordinatesPointsCurrentFigure[i].shiftRight(1);
        }
    }

    /**
     *  Перемещает фигуру в крайне левое положение
     */
    private void moveFigureToLeftBorder(){
        int leftEdgeDistance = getLeftPointOfFigure().getX();
        int topEdgeDistance = SIZE - getTopPointOfFigure().getY();
        for (Point point : coordinatesPointsCurrentFigure) {
            point.move(point.getX() - leftEdgeDistance, point.getY() + topEdgeDistance);
        }
        show();
    }

    /**
     * Возвращает крайне верхнюю точку фигуры
     * @return
     */
    private Point getTopPointOfFigure() {
        Point topPoint = coordinatesPointsCurrentFigure[0];
        for (int i = 1; i < coordinatesPointsCurrentFigure.length; i++) {
            Point temp = coordinatesPointsCurrentFigure[i];
            if (temp.getY() > topPoint.getY()) {
                topPoint = temp;
            }
        }
        return topPoint;
    }

    /**
     * Возвращает крайне нижюю точку фигуры
     * @return
     */
    private Point getBottomPointOfFigure() {
        Point bottomPoint = coordinatesPointsCurrentFigure[0];
        for (int i = 1; i < coordinatesPointsCurrentFigure.length; i++) {
            Point temp = coordinatesPointsCurrentFigure[i];
            if (temp.getY() < bottomPoint.getY()) {
                bottomPoint = temp;
            }
        }
        return bottomPoint;
    }

    /**
     * Возвращает крайне левую точку фигуры
     * @return
     */
    private Point getLeftPointOfFigure() {
        Point leftPoint = coordinatesPointsCurrentFigure[0];
        for (int i = 1; i < coordinatesPointsCurrentFigure.length; i++) {
            Point temp = coordinatesPointsCurrentFigure[i];
            if (temp.getX() < leftPoint.getX()) {
                leftPoint = temp;
            }
        }
        return leftPoint;
    }

    /**
     * Возвращает крайне правую точку фигуры
     * @return
     */
    private Point getRightPointOfFigure() {
        Point rightPoint = coordinatesPointsCurrentFigure[0];
        for (int i = 1; i < coordinatesPointsCurrentFigure.length; i++) {
            Point temp = coordinatesPointsCurrentFigure[i];
            if (temp.getX() > rightPoint.getX()) {
                rightPoint = temp;
            }
        }
        return rightPoint;
    }

    /**
     * Печатает координаты каждой точки фигуры
     */
    private void show() {
        StringBuilder builder = new StringBuilder();
        for (Point point : coordinatesPointsCurrentFigure) {
            builder.append("{x=")
                    .append(point.getX())
                    .append(" y=")
                    .append(point.getY())
                    .append("}\n");
        }
        System.out.println(builder.toString());
    }
}
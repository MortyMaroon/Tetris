package com.codenjoy.dojo.bot;

import com.codenjoy.dojo.services.Command;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.services.Rotation;
import com.codenjoy.dojo.tetris.client.Board;
import com.codenjoy.dojo.tetris.client.GlassBoard;
import com.codenjoy.dojo.tetris.model.Elements;

import java.util.*;

/**
 * Клас обрабатывающий текущее представление доски для нахождения наилучшего решения
 * Содержит методы для опеределения различных показателей все возможных вариантов постановки фигур
 */
public class Bot {
    private final int SIZE = 17;
    private Board board;
    private GlassBoard glassBoard;
    private List<Point> listBusyPoints;
    private Elements currentElement;
    private Point[] coordinatesPointsCurrentFigure;
    private int figureWidth;
    private ArrayList<State> listOfStagingOptions;
    private Position position = Position.UP;
    private int numberPossiblePosition = 0;
    private double averageHeightBoard;
    private int maxHeightBoard;

    /**
     * Инициализирует поля для работы основных методов
     * @param board Игровое поле
     */
    private void initialize(Board board) {
        this.board = board;
        this.glassBoard = board.getGlass();
        this.listBusyPoints = board.getGlass().getFigures();
        this.currentElement = board.getCurrentFigureType();
        this.coordinatesPointsCurrentFigure = board.predictCurrentFigurePoints();
        this.figureWidth = getRightPointOfFigure().getX() - getLeftPointOfFigure().getX();
        this.listOfStagingOptions = new ArrayList();
        listBusyPoints.removeAll(Arrays.asList(coordinatesPointsCurrentFigure));
        this.averageHeightBoard = averageHeightBoard();
        this.maxHeightBoard = maxHeightBoard();

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
            case YELLOW:
                checkingOptions(numberPossiblePosition);
                listOfStagingOptions.sort(Comparator.comparingDouble(State::getPoints));
                System.out.println(listOfStagingOptions);
                result = createMove(chooseBestMove());
                break;

            case BLUE:
            case RED:
            case GREEN:

                for (int external = 0; external < 2; external++) {
                    checkingOptions(numberPossiblePosition);
                    turnFigure(external);
                    System.out.println("Повернули");
                }
                listOfStagingOptions.sort(Comparator.comparingDouble(State::getPoints));
                System.out.println(listOfStagingOptions);
                result = createMove(chooseBestMove());
                break;

            case ORANGE:
            case CYAN:
            case PURPLE:
                for (int external = 0; external < 4; external++) {
                    checkingOptions(numberPossiblePosition);
                    turnFigure(external);
                    System.out.println("Повернули");
                }
                listOfStagingOptions.sort(Comparator.comparingDouble(State::getPoints));
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
        ArrayList<State> bestStates = new ArrayList<>();
        int positionElement = listOfStagingOptions.size() - 1;
        State bestState = listOfStagingOptions.get(positionElement--);
        bestStates.add(bestState);
        double maxPoint = bestState.getPoints();
        while (positionElement != 0) {
            bestState = listOfStagingOptions.get(positionElement--);
            if (bestState.getPoints() == maxPoint) {
                bestStates.add(bestState);
            } else {
                break;
            }
        }
        positionElement = new Random().nextInt(bestStates.size());
        return bestStates.get(positionElement);
    }

    /**
     * Проходит все возможные положения текущей фигуры с подсчетом внутренних балов для каждого положения
     * @param numberPossiblePosition Количество возможных положений текущей фигуры
     */
    private void checkingOptions(int numberPossiblePosition) {
        moveFigureToLeftBorder();
        for (int internal = 0; internal <= numberPossiblePosition; internal++) {
            while (getBottomPointOfFigure().getY() > 0 && checkPosition()) {
                moveFigureBottom();
            }
            listOfStagingOptions.add(new State(position, countingPoints(internal), internal));
            moveFigureToTopBorder();
            moveFigureRight();
        }
    }

    /**
     * Изменяет координаты точек фигуры в зависимости от переданного параметра ващения
     * корректирует данные ширины фигуры и количества возможных постановок
     * @param direction Ключ вращения фигуры
     */
    private void turnFigure(int direction) {
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
        System.out.println("Выбрали позицию: " + state.toString());
        List<Command> result = new ArrayList<>();
        switch (state.getPosition()) {
            case UP:
                coordinatesPointsCurrentFigure = board.predictCurrentFigurePoints();
                break;
            case RIGHT:
                result.add(Command.ROTATE_CLOCKWISE_90);
                coordinatesPointsCurrentFigure = board.predictCurrentFigurePoints(Rotation.CLOCKWIZE_90);
                break;
            case DOWN:
                result.add(Command.ROTATE_CLOCKWISE_180);
                coordinatesPointsCurrentFigure = board.predictCurrentFigurePoints(Rotation.CLOCKWIZE_180);
                break;
            case LEFT:
                result.add(Command.ROTATE_CLOCKWISE_270);
                coordinatesPointsCurrentFigure = board.predictCurrentFigurePoints(Rotation.CLOCKWIZE_270);
                break;
        }
        int place = state.getPlace();
        int anchorPlace = getLeftPointOfFigure().getX();
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
    private double countingPoints(int position) {
        double numberEmptyPoint = countingEmptyPoint(getLeftPointOfFigure().getX());
        double towerHeight = getTopPointOfFigure().getY();
        double numberRemovedLines = countingBrokenLines();
        if (maxHeightBoard < 14) {
            if (numberRemovedLines >= 3) {
                return 1000;
            }
            if (position == 0) {
                return 0;
            }
            numberEmptyPoint = (SIZE - numberEmptyPoint) * 2.5;
            towerHeight = (SIZE - towerHeight) * 1.5;
        } else {
            numberEmptyPoint = (SIZE - numberEmptyPoint) * 1.25;
            towerHeight = (SIZE - towerHeight) * 1.40;
        }
        numberRemovedLines = Math.pow(numberRemovedLines,2) * 2 + 3 ;

        return numberEmptyPoint + towerHeight + numberRemovedLines + position * 0.25;
    }

    private double averageHeightBoard(){
        int averageHeightBoard = 0;
        Point tempPoint;
        for (int column = 1; column <= SIZE; column++) {
            for (int row = SIZE; row >= 0; row--) {
                tempPoint = new PointImpl(column,row);
                if (listBusyPoints.contains(tempPoint)) {
                    averageHeightBoard = averageHeightBoard + row;
                    break;
                }
            }
            averageHeightBoard++;
        }
        System.out.println("Средняя высота доски: " + averageHeightBoard / SIZE);
        return averageHeightBoard / SIZE;
    }

    private int maxHeightBoard(){
        int maxHeightBoard = 0;
        Point tempPoint;
        for (int column = 1; column <= SIZE; column++) {
            for (int row = SIZE; row >= 0; row--) {
                tempPoint = new PointImpl(column,row);
                if (listBusyPoints.contains(tempPoint)) {
                    if (tempPoint.getY() > maxHeightBoard) {
                        maxHeightBoard = tempPoint.getY();
                    }
                    break;
                }
            }
        }
        System.out.println("Максимальная высота доски высота доски: " + maxHeightBoard);
        return maxHeightBoard;
    }

    /**
     * Считает количтво образовавшихся пустых точек под местом постановки фигуры
     * @param leftEdge Левая граница фигуры
     * @return Число пустых точек
     */
    private int countingEmptyPoint(int leftEdge) {
        int numberEmptyPoint = 0;
        Point point;
        for (int row = leftEdge; row <= getRightPointOfFigure().getX(); row++) {
            point = lowestPointInColumn(row);
            int bottom = point.getY();
            for (int column = bottom - 1; column >= 0; column--) {
                if (glassBoard.isFree(row,column)) {
                    numberEmptyPoint++;
                }
            }
        }
        return numberEmptyPoint;
    }

    /**
     * Считает возможное количество уничтоженных линий
     * @return
     */
    private int countingBrokenLines() {
        int numbersLines = 0;
        for (int row = getBottomPointOfFigure().getY(); row <= getTopPointOfFigure().getY(); row++) {
            numbersLines++;
            for (int column = 0; column <= SIZE; column++) {
                if (glassBoard.isFree(column,row)) {
                    if (!checkPointPartFigure(column,row)) {
                        numbersLines--;
                        break;
                    }
                }
            }
        }
        return numbersLines;
    }

    /**
     * Проверяет яавляется ли переданная координаты точки частью фигуры
     * @param column Столбец
     * @param row Строка
     * @return true если переданные данные являются частью фигуры
     */
    private boolean checkPointPartFigure(int column, int row) {
        for (Point point : coordinatesPointsCurrentFigure) {
            if (point.itsMe(column, row)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Возвращает самую нижнюю точку фигуры в переданном столбике
     * @param row Столбик в котором нужно найти нижнюю точку
     * @return
     */
    private Point lowestPointInColumn(int row) {
        Point point = getLeftPointOfFigure();
        for (int i = 1; i < coordinatesPointsCurrentFigure.length; i++) {
            Point temp = coordinatesPointsCurrentFigure[i];
            if (temp.getX() == row) {
                if (temp.getY() < point.getY()) {
                    point = temp;
                }
            }
        }
        return point;
    }

    /**
     * Проверяет каждую точку фигуры на вхождение в список координает занятых точек
     * @return true если нет вхождений
     */
    private boolean checkPosition() {
        for (Point point : coordinatesPointsCurrentFigure) {
            Point temp = new PointImpl();
            temp.setX(point.getX());
            temp.setY(point.getY() - 1);
            if (listBusyPoints.contains(temp)) {
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
     * Перемещает фигуру в крайне левое положение
     */
    private void moveFigureToLeftBorder(){
        int leftEdgeDistance = getLeftPointOfFigure().getX();
        int topEdgeDistance = SIZE - getTopPointOfFigure().getY();
        for (Point point : coordinatesPointsCurrentFigure) {
            point.move(point.getX() - leftEdgeDistance, point.getY() + topEdgeDistance);
        }
    }

    /**
     * Перемещает фигуру в крайне верхнее положение
     */
    private void moveFigureToTopBorder(){
        int topEdgeDistance = SIZE - getTopPointOfFigure().getY();
        for (Point point : coordinatesPointsCurrentFigure) {
            point.move(point.getX(), point.getY() + topEdgeDistance);
        }
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
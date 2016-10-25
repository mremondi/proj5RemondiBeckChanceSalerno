/*
 * File: CompositionManager.java
 * Names: Graham Chance, Charlie Beck, Ryan Salerno, Mike Remondi
 * Class: CS361
 * Project: 5
 * Due Date: October 23, 2016
 */

package proj5BeckChanceRemondiSalerno;

import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import proj5BeckChanceRemondiSalerno.CompositionActions.CompositionAction;
import proj5BeckChanceRemondiSalerno.Controllers.TempoLineController;
import proj5BeckChanceRemondiSalerno.Models.*;
import proj5BeckChanceRemondiSalerno.Views.NoteGroupablePane;
import proj5BeckChanceRemondiSalerno.Views.NoteRectangle;
import java.util.*;

/**
 * This class models a composition sheet manager.
 * Deals with adding and manipulating all the data
 * regarding the composition
 *
 * Singleton
 *
 * @author Graham Chance
 * @author Charlie Beck
 * @author Ryan Salerno
 * @author Mike Remondi
 */
public class CompositionManager {

    /**
     * The shared instance of the singleton
     */
    private static CompositionManager instance = null;

    /**
     * The map for matching note models to their views
     */
    private HashMap<NoteGroupable, NoteGroupablePane> noteGroupableRectsMap = new HashMap<>();

    /**
     * The controller for the tempo line
     */
    private TempoLineController tempoLineController;

    /**
     * The composition view
     */
    private Pane composition;

    /**
     * The index of the current selected instrument
     */
    private int currentSelectedInstrumentIndex;

    /**
     * The composition player for playing notes
     */
    private CompositionPlayer compositionPlayer = new CompositionPlayer();

    private  Stack<CompositionAction> redoActions = new Stack<>();
    private  Stack<CompositionAction> undoActions = new Stack<>();

    /**
     * constructor
     * hidden from public to enforce singleton
     */
    private CompositionManager() {}

    /**
     * shared instance getter
     * @return The single shared instance
     */
    public static synchronized CompositionManager getInstance(){
        if (instance == null){
            instance = new CompositionManager();
        }
        return instance;
    }

    /**
     * Setter for tempo line controller
     * @param tempoLineControllerontroller the new tempo line controller
     */
    public void setTempoLineController(TempoLineController tempoLineControllerontroller){
        this.tempoLineController = tempoLineControllerontroller;
    }

    /**
     * Setter for the composition view
     * @param composition The new composition view
     */
    public void setComposition(Pane composition){
        this.composition = composition;
    }

    /**
     * Getter for the composition view
     * @return The composition view
     */
    public Pane getComposition(){
        return this.composition;
    }

    /**
     * Maps channel numbers to specific instrument color association
     */
    public Paint getInstrumentColor(int instrumentIndex) {
        switch (instrumentIndex) {
            case 0:
                return Color.GRAY;
            case 1:
                return Color.GREEN;
            case 2:
                return Color.BLUE;
            case 3:
                return Color.GOLDENROD;
            case 4:
                return Color.MAGENTA;
            case 5:
                return Color.DEEPSKYBLUE;
            case 6:
                return Color.BLACK;
            case 7:
                return Color.BROWN;
        }
        return null;
    }

    /**
     * Setter for current instrument
     * @param newInstrumentIndex The new instrument index
     */
    public void setInstrumentIndex(int newInstrumentIndex) {
        currentSelectedInstrumentIndex = newInstrumentIndex;
    }


    /**
     * Creates a visual representation of the the notes
     * in the composition sheet.
     *
     * @param xPos the input x position of the note
     * @param yPos the input y position of the note
     *
     * @return the note added
     */
    public NoteGroupable addNoteToComposition(double xPos, double yPos) {
        System.out.format("adding note at location (%f, %f)\n", xPos, yPos);
        if (yPos >= 0 && yPos < 1280) {
            Note note = new Note(xPos, yPos, 100, currentSelectedInstrumentIndex);
            addGroupable(note);
            selectGroupable(note);
            return note;
        }
        return null;
    }


    /**
     * Creates a note group from current selected groupables
     * @return
     */
    public Optional<NoteGroup> createNoteGroup() {
        System.out.println("grouping");
        ArrayList<NoteGroupable> notesToGroup = new ArrayList<>();
        for (NoteGroupable note : getGroupables()) {
            if (note.isSelected()) {
                notesToGroup.add(note);
            }
        }
        if (notesToGroup.size() < 2) {
             return Optional.empty();
        }
        for (NoteGroupable noteGroupable : notesToGroup) {
            composition.getChildren().remove(noteGroupableRectsMap.get(noteGroupable));
            noteGroupableRectsMap.remove(noteGroupable);
        }

        NoteGroup group = new NoteGroup(notesToGroup);
        NoteGroupablePane rect = createNoteGroupablePane(group);
        noteGroupableRectsMap.put(group, rect);
        composition.getChildren().add(rect);
        selectGroupable(group);

        return Optional.of(group);
    }


    /**
     * Ungroups all current selected groups
     */
    public void ungroupSelectedGroup() {
        ArrayList<NoteGroupable> groupsToUnGroup = new ArrayList<>();
        for (NoteGroupable noteGroupable : noteGroupableRectsMap.keySet()) {
            if (noteGroupable.isSelected() && noteGroupable instanceof NoteGroup) {
                groupsToUnGroup.add(noteGroupable);
            }
        }

        if (groupsToUnGroup.size() != 1) { return; }

        ArrayList<NoteGroupable> subNoteGroupables = ((NoteGroup) groupsToUnGroup.get(0)).getNoteGroupables();

        composition.getChildren().remove(noteGroupableRectsMap.get(groupsToUnGroup.get(0)));
        noteGroupableRectsMap.remove(groupsToUnGroup.get(0));

        for (NoteGroupable subNoteGroupable : subNoteGroupables) {
            addGroupable(subNoteGroupable);
        }
    }

    public void actionCompleted(CompositionAction action) {
        // add to undo stack
        undoActions.push(action);
        // remove everything from redo stack
        redoActions.removeAllElements();
    }

    public void undoLastAction() {
        undoActions.pop().undo();
    }

    public void redoLastUndoneAction() {
        redoActions.pop().redo();
    }

    /**
     * Finds a Note, if one exists, where the mouse click is inside of
     * its rectangle.
     *
     * @param x MouseEvent x coordinate
     * @param y MouseEvent y coordinate
     */
    public Optional<NoteGroupable> getGroupableAtPoint(double x, double y) {
        for (NoteGroupable noteGroupable : this.getGroupables()) {
            if (getGroupPane(noteGroupable).getIsInBounds(x, y)) {
                return Optional.of(noteGroupable);
            }
        }
        return Optional.empty();
    }

    /**
     * Getter for the groupable models
     * @return All groupable models in composition
     */
    public Set<NoteGroupable> getGroupables() {
        return noteGroupableRectsMap.keySet();
    }


    /**
     * Selects a note groupable
     * @param noteGroupable The groupable to select
     */
    public void selectGroupable(NoteGroupable noteGroupable) {
        noteGroupable.setSelected(true);
        noteGroupableRectsMap.get(noteGroupable).setSelected(true);
    }

    /**
     * Deselects a note groupable
     * @param noteGroupable The groupable to deselect
     */
    public void deselectNote(NoteGroupable noteGroupable){
        noteGroupable.setSelected(false);
        noteGroupableRectsMap.get(noteGroupable).setSelected(false);
    }

    /**
     * Getter for group pane corresponding to a groupable
     * @param noteGroupable The groupable to get the view for
     * @return The view for the groupable
     */
    public NoteGroupablePane getGroupPane(NoteGroupable noteGroupable){
        return noteGroupableRectsMap.get(noteGroupable);
    }

    /**
     * Selects the groupables contained in an area
     * @param bounds the area to select in
     */
    public void selectNotesIntersectingRectangle(Bounds bounds) {
        for (NoteGroupable noteGroupable : getGroupables()) {
            if (getGroupPane(noteGroupable).getIsInRectangleBounds(bounds.getMinX(), bounds.getMinY(),
                    bounds.getMaxX(), bounds.getMaxY())) {
                selectGroupable(noteGroupable);
            }
        }
    }

    /**
     * Clears the list of selected notes
     */
    public void clearSelectedNotes() {
        for (NoteGroupable noteGroupable : noteGroupableRectsMap.keySet()) {
            if (noteGroupable.isSelected()){
                this.deselectNote(noteGroupable);
            }
        }
    }

    /**
     * Plays the sequence of notes and animates the TempoLine.
     */
    public void play(){
        compositionPlayer.play(getNotes());
        double stopTime = this.calculateStopTime();
        this.tempoLineController.updateTempoLine(stopTime);
        this.tempoLineController.playAnimation();
    }

    /**
     * Stops the midiPlayer and hides the tempoLine.
     */
    public void stop(){
        compositionPlayer.stop();
        this.tempoLineController.stopAnimation();
        this.tempoLineController.hideTempoLine();
    }

    /**
     * Deletes selected groupables
     */
    public void deleteSelectedGroupables() {
        ArrayList<NoteGroupable> groupablesToDelete = new ArrayList<>();
        for (NoteGroupable noteGroupable : getGroupables()) {
            if (noteGroupable.isSelected()) {
                NoteGroupablePane groupPane = noteGroupableRectsMap.get(noteGroupable);
                composition.getChildren().remove(groupPane);
                groupablesToDelete.add(noteGroupable);
            }
        }

        for (NoteGroupable noteGroupable : groupablesToDelete) {
            noteGroupableRectsMap.remove(noteGroupable);
        }
    }

    /**
     * Adds a
     * @param noteGroupable
     */
    private void addGroupable(NoteGroupable noteGroupable) {
        NoteGroupablePane groupPane = createNoteGroupablePane(noteGroupable);
        noteGroupableRectsMap.put(noteGroupable, groupPane);
        composition.getChildren().add(groupPane);
        selectGroupable(noteGroupable);
    }


    /**
     * Gets the list of notes.
     *
     * @return ArrayList of MusicalNotes
     */
    private ArrayList<Note> getNotes() {
        ArrayList<Note> notes = new ArrayList<>();
        for (NoteGroupable group : noteGroupableRectsMap.keySet()) {
            for (Note note : group.getNotes()) {
                notes.add(note);
            }
        }
        return notes;
    }


    /**
     * Calculates the stop time for the composition created
     *
     * @return stopTime
     */
    private double calculateStopTime() {
        double stopTime = 0.0;
        for (Note note : this.getNotes()) {
            if (stopTime < note.getStartTick() + note.getDuration()) {
                stopTime = note.getStartTick() + note.getDuration();
            }
        }
        return stopTime;
    }

    /**
     * Creates a new NoteGroupablePane based on a group
     * @param noteGroupable The groupable to create a pane for
     * @return the new groupable pane
     */
    private NoteGroupablePane createNoteGroupablePane(NoteGroupable noteGroupable) {
        NoteGroupablePane groupablePane = new NoteGroupablePane();
        groupablePane.setMinWidth(noteGroupable.getEndTick() - noteGroupable.getStartTick());
        groupablePane.setMinHeight(noteGroupable.getMaxPitch() - noteGroupable.getMinPitch() + 10);
        int x = noteGroupable.getStartTick();
        int y = (127- noteGroupable.getMaxPitch()) * 10;
        System.out.format("adding notegroup pane at (%d, %d)\n", x,y);
        groupablePane.setLayoutX(x);
        groupablePane.setLayoutY(y);

        if (noteGroupable instanceof Note) {
            Note note = (Note) noteGroupable;
            NoteRectangle noteBox = new NoteRectangle(noteGroupable.getDuration(),0,0);
            noteBox.setFill(getInstrumentColor(note.getChannel()));
            groupablePane.getChildren().add(noteBox);
            groupablePane.setContainsSingleNote(true);
            return groupablePane;
        } else {
            NoteGroup group = (NoteGroup) noteGroupable;
            for (NoteGroupable subNoteGroupable : group.getNoteGroupables()) {
                NoteGroupablePane subGroupRect = createNoteGroupablePane(subNoteGroupable);
                subGroupRect.setLayoutX(subGroupRect.getLayoutX()-groupablePane.getLayoutX() + 5);
                subGroupRect.setLayoutY(subGroupRect.getLayoutY()-groupablePane.getLayoutY() + 5);
                groupablePane.getChildren().add(subGroupRect);
            }

            return groupablePane;
        }
    }

}

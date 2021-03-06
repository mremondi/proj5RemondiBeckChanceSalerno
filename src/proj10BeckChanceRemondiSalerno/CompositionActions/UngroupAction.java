/*
 * File: UngroupAction.java
 * Names: Graham Chance, Charlie Beck, Ryan Salerno, Mike Remondi
 * Class: CS361
 * Project: 10
 * Due Date: December 19, 2016
 */

package proj10BeckChanceRemondiSalerno.CompositionActions;


import proj10BeckChanceRemondiSalerno.CompositionManager;
import proj10BeckChanceRemondiSalerno.Models.NoteGroup;
import proj10BeckChanceRemondiSalerno.Models.NoteGroupable;

/**
 * This class extends the CompositionAction abstract class and represents the action
 * of ungrouping notes in the Composition.
 *
 * @author Graham Chance
 * @author Charlie Beck
 * @author Ryan Salerno
 * @author Mike Remondi
 */
public class UngroupAction extends CompositionAction {
    /**
     * The note group
     */
    private NoteGroup group;

    /**
     * Constructor
     *
     * @param group the note group
     */
    public UngroupAction(NoteGroup group, CompositionManager compositionManager) {
        this.compositionManager = compositionManager;
        this.group = group;
    }

    /**
     * Redoes the ungrouping of notes.
     */
    public void redo() {
        compositionManager.ungroup(group);
    }

    /**
     * Undoes the ungrouping of notes by regrouping them.
     */
    public void undo() {
        for (NoteGroupable noteGroupable : group.getNoteGroupables()) {
            compositionManager.deleteGroupable(noteGroupable);
        }
        compositionManager.addGroupable(group);
    }

}

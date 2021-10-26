/*
 * Zettelkasten - nach Luhmann
 * Copyright (C) 2001-2015 by Daniel Lüdecke (http://www.danielluedecke.de)
 * 
 * Homepage: http://zettelkasten.danielluedecke.de
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Dieses Programm ist freie Software. Sie können es unter den Bedingungen der GNU
 * General Public License, wie von der Free Software Foundation veröffentlicht, weitergeben
 * und/oder modifizieren, entweder gemäß Version 3 der Lizenz oder (wenn Sie möchten)
 * jeder späteren Version.
 * 
 * Die Veröffentlichung dieses Programms erfolgt in der Hoffnung, daß es Ihnen von Nutzen sein 
 * wird, aber OHNE IRGENDEINE GARANTIE, sogar ohne die implizite Garantie der MARKTREIFE oder 
 * der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Details finden Sie in der 
 * GNU General Public License.
 * 
 * Sie sollten ein Exemplar der GNU General Public License zusammen mit diesem Programm 
 * erhalten haben. Falls nicht, siehe <http://www.gnu.org/licenses/>.
 */

package de.danielluedecke.zettelkasten.tasks;

import bibtex.dom.BibtexEntry;
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.database.Daten;
import de.danielluedecke.zettelkasten.database.TasksData;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Luedeke
 */
public class RefreshBibTexTask extends org.jdesktop.application.Task<Object, Void> {
    /**
     * Reference to the main data class
     */
    private final Daten daten;
    /**
     * the table model from the main window's jtable, passed as parameter
     */
    private final BibTeX bibTeX;
    private final TasksData tasksData;

    private final javax.swing.JDialog parentDialog;
    /**
     * get the strings for file descriptions from the resource map
     */
    private final org.jdesktop.application.ResourceMap resourceMap =
        org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).
        getContext().getResourceMap(RefreshBibTexTask.class);

    RefreshBibTexTask(org.jdesktop.application.Application app, javax.swing.JDialog parent, 
            javax.swing.JLabel label, TasksData tasksData, Daten daten, BibTeX bibTeX) {
        // Runs on the EDT.  Copy GUI state that
        // doInBackground() depends on from parameters
        // to createLinksTask fields, here.
        super(app);

        this.daten = daten;
        this.bibTeX = bibTeX;
        this.tasksData = tasksData;
        parentDialog = parent;
        // init status text
        label.setText(resourceMap.getString("msgBibTexRefresh"));
    }

    @Override protected Object doInBackground() {
        // get attached entries
        ArrayList<BibtexEntry> attachedbibtexentries = bibTeX.getEntriesFromAttachedFile();
        // for progress bar
        int cnt = 0;
        int length = attachedbibtexentries.size();
        // amount of updated entries
        int updateCount = 0;
        StringBuilder updatedAuthors = new StringBuilder();
        // iterate all new entries
        for (BibtexEntry attachedbibtexentry : attachedbibtexentries) {
            // do we have this entry?
            String bibkey = attachedbibtexentry.getEntryKey();
            if (bibTeX.hasEntry(bibkey)) {
                // if yes, update it
                bibTeX.setEntry(bibkey, attachedbibtexentry);
                // retrieve author position 
                int aupos = daten.getAuthorBibKeyPosition(bibkey);
                // check if we have author already
                if (aupos != -1) {
                    // get current author
                    String oldAuthor = daten.getAuthor(aupos);
                    // get formatted author
                    String updatedAuthor = bibTeX.getFormattedEntry(attachedbibtexentry, true);
                    // update author data, if it differs
                    if (!oldAuthor.equals(updatedAuthor)) {
                        // update author in data base
                        daten.setAuthor(aupos, updatedAuthor);
                        // copy info to string
                        updatedAuthors.append(updatedAuthor)
                                .append(" (bibkey: ")
                                .append(bibkey)
                                .append(")")
                                .append(System.lineSeparator());
                        updateCount++;
                    }
                }
            }
            // update progressbar
            setProgress(cnt++, 0, length);
        }
        // add all new entries to data base
        int newentries = bibTeX.addEntries(attachedbibtexentries);

        tellUser(updateCount, newentries);

        // log info about updates authors
        tasksData.setUpdatedAuthors(updatedAuthors.toString());
        return null;
    }

    private void tellUser(int updateCount, int newentries) {
        if (newentries > 0 || updateCount > 0) {
            JOptionPane.showMessageDialog(null, 
                    resourceMap.getString("importMissingBibtexEntriesText", 
                            String.valueOf(newentries),
                            String.valueOf(updateCount)), 
                    "BibTeX-Import",
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    @Override protected void succeeded(Object result) {
        daten.setAuthorlistUpToDate(false);
    }

    @Override
    protected void finished() {
        super.finished();
        // and close window
        parentDialog.dispose();
        parentDialog.setVisible(false);
    }
}

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

package de.danielluedecke.zettelkasten;

import de.danielluedecke.zettelkasten.util.Constants;
import de.tobiashilbricht.thotandamun.ThotandamunApp;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

/**
 *
 * @author Tobias Hilbricht
 */

public class HelpBox extends JDialog {

    private JEditorPane editorPane;
    private JScrollPane scrollPane;

    /**
     * This creates the help window
     *
     * @param frame for parent window
     */
    public HelpBox(Frame frame) {
        super(frame, false); // false for non-modal window, important for a help window

        // set application icon
        setIconImage(Constants.zknicon.getImage());
        // what to do on close
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        // define resourceMap for strings
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ThotandamunApp.class).getContext().getResourceMap(HelpBox.class);
        // set title depending on language in use
        setTitle(resourceMap.getString("FormHelpBox.title")); // NOI18N
        // these codelines add an escape-listener to the dialog. so, when the user
        // presses the escape-key, the same action is performed as if the user
        // presses the cancel button...
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener cancelAction = new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                setVisible(false);
                dispose();
            }
        };
        // editorPane for HTML content
        editorPane = new JEditorPane();
        editorPane.setEditable(false); // Just view, do not touch
        editorPane.setBorder(null);
        // here reference to resource key in HelpBox.form to get values of HelpBox.properties, old name of editor panel
        editorPane.setContentType(resourceMap.getString("editorPane.contentType")); // NOI18N
        editorPane.setName("editorPane"); // NOI18N
        editorPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent evt) {
                editorPaneHyperlinkUpdate(evt);
            }
        });

        // scrollPane to show the editorPane
        scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setName("jScrollPane1"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
        );

        pack();

        getRootPane().registerKeyboardAction(cancelAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        URL helpText = org.jdesktop.application.Application.getInstance(ThotandamunApp.class).getClass().getResource("/de/danielluedecke/zettelkasten/resources/settingshelp/settingshelp.html");

        // Show help in editorPane
        try {
            editorPane.setPage(helpText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void editorPaneHyperlinkUpdate(HyperlinkEvent evt) {//GEN-FIRST:event_jEditorPane1HyperlinkUpdate
        // retrieve the event type, e.g. if a link was clicked by the user
        HyperlinkEvent.EventType typ = evt.getEventType();
        // get the description, to check whether we have a file or a hyperlink to a website
        String linktype = evt.getDescription();
        // if the link was clicked, proceed
        if (typ == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                // if the string contains "http://", we assume we have a webpage
                if (linktype.contains("http://") || linktype.contains("https://")) {
                    Desktop.getDesktop().browse(new URI(evt.getURL().toString()));
                }
                // if the string contains "http://", we assume we have a webpage
                else if (linktype.contains("mailto:")) {
                    Desktop.getDesktop().mail(new URI(evt.getURL().toString()));
                } else if (linktype.startsWith("#")) {
                    editorPane.scrollToReference(linktype.substring(1));
                }
            } catch (IOException | URISyntaxException e) {
                Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
            }
        }

    }
}

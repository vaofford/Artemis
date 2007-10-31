/* JExtendedComboBox.java
 *
 * created: 2007
 *
 * This file is part of Artemis
 *
 * Copyright (C) 2007  Genome Research Limited
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 **/

package uk.ac.sanger.artemis.components.genebuilder;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

import org.gmod.schema.cv.CvTerm;


/**
 * JComboBox with horizontal scrollbar
 */
public class JExtendedComboBox extends JComboBox
{ 
  /** */
  private static final long serialVersionUID = 1L;
  public static String SEPARATOR = "SEPARATOR";
  
  public JExtendedComboBox(String str[])
  { 
    super(str);
    setRenderer(new ComboBoxRenderer());
    //setUI(new ComboUI());
    setHorizontalScrollBar();
  } 
  
  public JExtendedComboBox(Vector vector)
  { 
    super(vector);
    setRenderer(new ComboBoxRenderer());
    //setUI(new ComboUI());
    setHorizontalScrollBar();
  } 

  private void setHorizontalScrollBar()
  { 
    BasicComboPopup popup = (BasicComboPopup)getUI().getAccessibleChild(this,0);//Popup

    if(popup==null)
      return; 
   
    for(int i=0; i<popup.getComponentCount(); i++)
    {
      Component comp = popup.getComponent(i);
      if(comp instanceof JScrollPane) 
      {
        JScrollPane scrollpane = (JScrollPane)comp;
        scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        return;
      }
    }
  }
  
  class ComboBoxRenderer extends JLabel implements ListCellRenderer 
  {
    private static final long serialVersionUID = 1L;
    private JSeparator separator;

    public ComboBoxRenderer() 
    {
      setOpaque(true);
      setBorder(new EmptyBorder(1, 1, 1, 1));
      separator = new JSeparator(JSeparator.HORIZONTAL);
    }

    public Component getListCellRendererComponent(
        final JList list, final Object value,
        final int index,  final boolean isSelected,
        final boolean cellHasFocus) 
    {
      String str;
      if(value instanceof String)
        str = (value == null) ? "" : value.toString();
      else
        str = ((CvTerm)value).getName();

      if (SEPARATOR.equals(str))
        return separator;
 
      if (isSelected) 
      {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } 
      else 
      {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      setFont(list.getFont());
      setText(str);
      return this;
    }
  }
  
  public class ComboUI extends BasicComboBoxUI
  {
    public ComboUI()
    {
      super();
      setUI(JExtendedComboBox.this.getUI());
    }
    
    protected ComboPopup createPopup()
    {
      BasicComboPopup popup = new ComboBoxPopup(JExtendedComboBox.this);
      return popup;
    }
  }
  
  class ComboBoxPopup extends BasicComboPopup 
  {
    public ComboBoxPopup(JExtendedComboBox combo)
    {
      super(combo);
    }

    private static final long serialVersionUID = 1L;

    protected JScrollPane createScroller() 
    {
      return new JScrollPane(list,
       ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
    }  
  }

  
  public static void main(String args[])
  {
    final String options[] = { "<PREV", "CANCEL", "NEXT>"};   
    
    Vector terms = new Vector();
    terms.add("Test test test test test test");
    terms.add("Test test test test test test test test test test");
    terms.add("Test test test test test test test test test test test"+ 
        " test test test test test test test test test test test test test"+
        " test test test test test test test test test test test test test"+
        " test test test test test test test test test test test test test"+
        " test test test test test test test test test test test test test"+
        " test test test test test test test test test test test test test test test test test test");
    JExtendedComboBox term_list = new JExtendedComboBox(terms);

    Dimension d = new Dimension(500,term_list.getPreferredSize().height);
    term_list.setPreferredSize(d);
    term_list.setMaximumSize(d);
   
    JOptionPane.showOptionDialog(null, term_list,
        "CV term selection",
         JOptionPane.YES_NO_CANCEL_OPTION,
         JOptionPane.QUESTION_MESSAGE,
         null,
         options,
         options[2]);
  }
  
}
/*
 *
 * created: Wed Aug 3 2004
 *
 * This file is part of Artemis
 *
 * Copyright(C) 2000  Genome Research Limited
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or(at your option) any later version.
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
 */

package uk.ac.sanger.artemis.editor;

import java.awt.Point;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;

import uk.ac.sanger.artemis.util.WorkingGZIPInputStream;

public class FastaTextPane extends JScrollPane
{
  private JTextArea textArea;
  private Vector hitInfoCollection = null;
  private String format = null;

  public FastaTextPane(String dataFile)
  {
    super();
    //read fasta file

    format = getResultsFormat(dataFile);
    StringBuffer contents = null;

    if(format.equals("fasta"))
      contents = readFASTAFile(dataFile,format);
    else if(format.equals("blastp"))
      contents = readBLASTPFile(dataFile,format);

    Font font = new Font("Monospaced",Font.PLAIN,12);

    textArea = new JTextArea(contents.toString());
    setTextAreaFont(font);
    textArea.setEditable(false);

    setViewportView(textArea);
    setPreferredSize(new Dimension(500,300));
  }

  /**
  *
  * Get the format of the results (e.g. FASTA or BLASTP).
  * @return format
  *
  */
  protected String getFormat()
  {
    return format;
  }

  protected void setTextAreaFont(Font f)
  {
    textArea.setFont(f);
  }
  
  protected InputStream getInputStream(String dataFile)
            throws IOException
  {
    FileInputStream inStream = new FileInputStream(dataFile);
    if(dataFile.endsWith(".gz"))
      return new WorkingGZIPInputStream(inStream);
    else
      return inStream;
  }

  /**
  *
  * Get the format of the results in a file. FASTA and
  * BLASTP are supported.
  *
  */
  protected String getResultsFormat(String dataFile)
  {
    File fn = new File(dataFile);
    InputStreamReader streamReader = null;
    BufferedReader buffReader = null;
    String line = null;
    String format = null;

    try
    {
      streamReader = new InputStreamReader(getInputStream(dataFile));
      buffReader = new BufferedReader(streamReader);
      while( (line = buffReader.readLine()) != null)
      {
        if(line.startsWith("BLASTP"))
        {
          format = "blastp";
          break;
        }
        else if(line.indexOf("FASTA") > -1)
        {
          format = "fasta";
          break;
        }
      }
      streamReader.close();
      buffReader.close();
    }
    catch (IOException ioe)
    {
      System.out.println("Cannot read file: " + dataFile);
    }
    
    return format;
  }


  protected StringBuffer readBLASTPFile(String dataFile, String format)
  {
    File fn = new File(dataFile);
    StringBuffer sbuff = new StringBuffer();

    InputStreamReader streamReader = null;
    BufferedReader buffReader = null;

    hitInfoCollection = new Vector();
    try
    {
      streamReader = new InputStreamReader(getInputStream(dataFile));
      buffReader = new BufferedReader(streamReader);

      String line = null;
      int textPosition = 0;
      int len     = 0;
      HitInfo hit = null;
      int ind1 = 0;

      while( (line = buffReader.readLine()) != null)
      {
        len = line.length()+1;
        sbuff.append(line+"\n");
        if(line.startsWith("Sequences producing significant alignments:"))
        {
          buffReader.readLine();
          while( !(line = buffReader.readLine()).equals("") )
          {
            textPosition += line.length()+1;
            sbuff.append(line+"\n");
  
            hit = new HitInfo(line,format);
            hitInfoCollection.add(hit);
          }

        }
        else if(line.startsWith(">"))  // start of alignment
        {
          String currentID = line;

          int ind = line.indexOf(" ");
          if(ind > -1)
            currentID = line.substring(1,ind);

          int ind2 = currentID.indexOf(":");
          if(ind2 > -1)
          {
            currentID = currentID.substring(ind2+1); 
          }

          if(hit != null)
            hit.setEndPosition(textPosition);

          hit = getHitInfo(currentID,hitInfoCollection);
          hit.setStartPosition(textPosition);


          String going = "";
          ind = line.indexOf("GO:");
          if(ind > -1)
            going = line.substring(ind+3);
          
          String nextLine = null;
          buffReader.mark(210);
          while((nextLine = buffReader.readLine()).indexOf("Length") == -1)
          {
            if(going.equals("") && ((ind = nextLine.indexOf("GO:")) > -1))
              going = nextLine.substring(ind+3);
            else if(!going.equals(""))
              going = going.concat(nextLine);
          }

          buffReader.reset();
          if(!going.equals(""))
            hit.setGO(going); 
        }
        else if( (ind1 = line.indexOf("Identities = ")) > -1)
        {
          ind1 = line.indexOf("(",ind1)+1;
          if(ind1 > -1)
            hit.setIdentity(line.substring(ind1,line.indexOf(")",ind1)).trim());
        }
        else if( (ind1 = line.indexOf("  Length = ")) > -1)
          hit.setLength(line.substring(ind1+11));

        textPosition += len;
      }

      if(hit != null)
        hit.setEndPosition(textPosition);

      streamReader.close();
      buffReader.close();

      GetzThread getz = new GetzThread(hitInfoCollection);
      getz.start();
    }
    catch (IOException ioe)
    {
      System.out.println("Cannot read file: " + dataFile);
    }
    return sbuff;
  }


  protected StringBuffer readFASTAFile(String dataFile, String format)
  {
    File fn = new File(dataFile);
    StringBuffer sbuff = new StringBuffer();

    InputStreamReader streamReader = null;
    BufferedReader buffReader = null;

    hitInfoCollection = new Vector();
    try
    {
      streamReader = new InputStreamReader(getInputStream(dataFile));
      buffReader = new BufferedReader(streamReader);

      String line = null;
      int textPosition = 0;
      int len    = 0;
      HitInfo hi = null;

      while( (line = buffReader.readLine()) != null)
      {
        len = line.length()+1;
        sbuff.append(line+"\n");  

        int ind1;

        if(line.startsWith("The best scores are:"))
        {
          while( !(line = buffReader.readLine()).equals("") )
          {
            textPosition += line.length()+1;
            sbuff.append(line+"\n");
            hitInfoCollection.add(new HitInfo(line,format)); 
          }
        }
        else if(line.startsWith(">>"))  // start of alignment
        {
          int ind = line.indexOf(" ");
          String currentID = line.substring(2,ind);
          
          if(hi != null)
            hi.setEndPosition(textPosition);

          hi = getHitInfo(currentID,hitInfoCollection);
          hi.setStartPosition(textPosition);
        }
        else if(line.startsWith("Smith-Waterman")) // Smith-Waterman
        {
          ind1 = line.indexOf("score:");
          int ind2;
          if(ind1 > -1)
          {
            ind2 = line.indexOf(";",ind1);
            hi.setScore(line.substring(ind1+6,ind2));
     
            ind1 = ind2+1;
            ind2 = line.indexOf("identity");
            if(ind2 > -1)
              hi.setIdentity(line.substring(ind1,ind2).trim());
          
            ind1 = line.indexOf("(",ind2);
            if(ind1 > -1)
            {
              ind2 = line.indexOf("ungapped)",ind1);
              hi.setUngapped(line.substring(ind1+1,ind2).trim());
            }

            ind1 = line.indexOf(" in ",ind2);
            ind2 = line.indexOf("(",ind1);
            if(ind1 > -1 && ind2 > -1)
              hi.setOverlap(line.substring(ind1+4,ind2).trim());
           
            ind1 = ind2+1;
            ind2 = line.indexOf(":",ind1);
            if(ind2 > -1)
              hi.setQueryRange(line.substring(ind1,ind2));

            ind1 = ind2+1;
            ind2 = line.indexOf(")",ind1);
            if(ind2 > -1)
              hi.setSubjectRange(line.substring(ind1,ind2)); 
          }
        }
        else if( (ind1 = line.indexOf(" E():")) > -1)
        {
          StringTokenizer tok = new StringTokenizer(line.substring(ind1+5));
          hi.setEValue(tok.nextToken().trim());
        }
 
        textPosition += len;
      }
  
      if(hi != null)
        hi.setEndPosition(textPosition);
   
      streamReader.close();
      buffReader.close();

      GetzThread getz = new GetzThread(hitInfoCollection);
      getz.start();
    }
    catch (IOException ioe)
    {
      System.out.println("Cannot read file: " + dataFile);
    }
    return sbuff;
  }

  protected Vector getHitCollection()
  {
    return hitInfoCollection;
  }

  protected HitInfo getHitInfo(String ID, Vector hitInfoCollection)
  {
    Enumeration hitInfo = hitInfoCollection.elements();
    
    while(hitInfo.hasMoreElements())
    {
      HitInfo hi = (HitInfo)hitInfo.nextElement();
      if(hi.getID().equals(ID))
        return hi;
    }

    return null;
  }

  class GetzThread extends Thread
  {
    private Vector hitInfoCollection;

    protected GetzThread(Vector hitInfoCollection)
    {
      this.hitInfoCollection = hitInfoCollection;
    }

    public void run()
    {
      int max = hitInfoCollection.size();
      if(max > 10)
        max = 10;

      for(int i=0; i<max; i++)
        DataCollectionPane.getzCall((HitInfo)hitInfoCollection.get(i),false);
    }

  }


  public void show(Object obj)
  {
    if(obj instanceof HitInfo)
    {
      HitInfo hit = (HitInfo)obj;

      int start = hit.getStartPosition();
      int end   = hit.getEndPosition();
//    textArea.moveCaretPosition(end);
      textArea.moveCaretPosition(start);

      Point pos  = getViewport().getViewPosition();
      Dimension rect = getViewport().getViewSize();
      double hgt = rect.getHeight()+pos.getY();
      pos.setLocation(pos.getX(),hgt);
      getViewport().setViewPosition(pos);
    }
  }
 

}


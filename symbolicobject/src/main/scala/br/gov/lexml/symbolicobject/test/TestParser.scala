package br.gov.lexml.symbolicobject.test

import br.gov.lexml.symbolicobject.parser.IdSource
import br.gov.lexml.symbolicobject.tipos.STipo
import br.gov.lexml.symbolicobject.parser.Parser
import br.gov.lexml.symbolicobject.parser.InputDocument
import javax.swing.JFrame
import javax.swing.JTextArea
import java.awt.BorderLayout
import javax.swing.JButton
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import br.gov.lexml.symbolicobject.parser.Paragrafos
import br.gov.lexml.symbolicobject.tipos.Tipos
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import br.gov.lexml.symbolicobject.impl.Documento

object TestParser extends App {
	val parser = new Parser(new IdSource {
	  var id : Long = 0
	  override def nextId(tipo : STipo) : Long = {
	    id = id + 1
	    id
	  }
	})
	val f = new JFrame("parser")
	val ta = new JTextArea(80, 50)
	ta.setLineWrap(true)
	f.getContentPane().add(ta,BorderLayout.CENTER)
	val button = new JButton("Parse")
	f.getContentPane().add(button,BorderLayout.SOUTH)
	var parsed : Documento[Unit] = 
  _
	button.addActionListener(new ActionListener() {
	  override def actionPerformed(ev : ActionEvent) {
	    val text = ta.getText().split("[\n\r]")
	    val pars = Paragrafos(text)
	    
	    val inputDocument = InputDocument(Tipos.DocProjetoLei,pars,"urn:lex:br:federal:lei:2006-12-01;11380")
	    println("Parágrafos:")
	    pars.paragrafos.zipWithIndex.foreach(l => println("   " +  l))
	    println("Blocks:")
	    pars.blocks.foreach(x => x.zipWithIndex.foreach(y => println("   " + y)))
	    println("XML:")
	    val xml = pars.elem.foreach(println)
	    println("Result:")
	    val res = parser.parse(inputDocument)
	    parsed = res.toOption.get
	    parsed.os.initTreeProperties()
	    println(parsed)
	  }
	})
	f.pack()
	f.addWindowListener(new WindowAdapter() {
	  override def windowClosing(e : WindowEvent) {
	    System.exit(1)
	  }
	})
	f.setVisible(true)
	
}
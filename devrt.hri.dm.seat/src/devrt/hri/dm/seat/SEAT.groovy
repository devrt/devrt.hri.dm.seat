package devrt.hri.dm.seat

import jp.go.aist.rtm.RTC.DataFlowComponentBase
import jp.go.aist.rtm.RTC.Manager
import jp.go.aist.rtm.RTC.buffer.RingBuffer
import jp.go.aist.rtm.RTC.port.ConnectorBase
import jp.go.aist.rtm.RTC.port.ConnectorDataListener
import jp.go.aist.rtm.RTC.port.ConnectorDataListenerType
import jp.go.aist.rtm.RTC.port.InPort
import jp.go.aist.rtm.RTC.port.OutPort
import jp.go.aist.rtm.RTC.port.ConnectorBase.ConnectorInfo

import org.omg.CORBA.portable.OutputStream
import org.xml.sax.SAXException

import RTC.*

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

import org.cyberneko.html.parsers.SAXParser

import org.eclipse.ui.PlatformUI
import org.eclipse.jface.dialogs.MessageDialog

/*
 * SEAT (yet another SEAT compatible state transition engine)
 * Copyright (C) 2013 Yosuke Matsusaka, MID Academic Promotions, Inc.
 * All rights reserved.
 * Licensed under the Eclipse Public License -v 1.0 (EPL)
 * http://www.opensource.org/licenses/eclipse-1.0.txt
 */

public class SEAT extends DataFlowComponentBase {

  def _data
  def _port
  
  def _agents
  
  def _states
  def _rules
  def _currentstate
  
  def _scriptfile
  def _scorelimit
  
  def _validator

  public SEAT(Manager manager) {
    super(manager)
    _data = [:]
    _port = [:]
    _agents = [:]
    _states = [:]
    _rules = [:]
    _currentstate = null
  }

  // The initialize action (on CREATED->ALIVE transition)
  @Override
  public ReturnCode_t onInitialize() {
    bindParameter("scriptfile", _scriptfile, "none")
    bindParameter("scorelimit", _scorelimit, "0.0")
    def factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
    def schema = factory.newSchema(getClass().getResourceAsStream("/xsd/seatml.xsd"))
    _validator = schema.newValidator()
    return super.onInitialize()
  }

  // The activated action (Active state entry action)
  @Override
  public ReturnCode_t onActivated(int ec_id) {
      URL scripturl = new URL(_scriptfile)
      BufferedReader reader = new BufferedReader(new InputStreamReader(scripturl.openStream()))
      StringWriter writer = new StringWriter()
      def line
      while((line = reader.readLine()) != null) {
        writer.write(line)
      }
      reader.close()
      def scriptdata = reader.toString()

      try {
        _validator.validate(new StreamSource(new StringReader(scriptdata)))
      } catch (SAXException e) {
        def shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
        MessageDialog.openError(shell, "Error", e.getLocalizedMessage())
        return ReturnCode_t.RTC_ERROR
      }

      def parser = new XmlSlurper(new SAXParser())
      def doc = parser.parse(_scriptfile)
      _agents = doc.general.agent.collect {
        new MapEntry(it.@name.text(), [it.@type.text(), it.@datatype.text()])
      }
      _states = doc.state.collect {
        new MapEntry(it.@name.text(), it.rule.collect {
          new MapEntry(it.@name.text(), [it.@type.text(), it.@datatype.text()])
        })
      }
      return super.onActivated(ec_id)
  }
  
  // The execution action that is invoked periodically
  @Override
  public ReturnCode_t onExecute(int ec_id) {
    return super.onExecute(ec_id)
  }

  def createInPort(String name, Class type) {
    def data = type.newInstance(new RTC.Time(0,0), null)
    def port = new InPort(name, data)
    port.addConnectorDataListener(ConnectorDataListenerType.ON_BUFFER_WRITE,
      new Listener(name, type, this))
    registerInPort(name, port)
    _port[name] = port
    _data[name] = data
  }

  def createOutPort(String name, Class type) {
    def data = type.newInstance(new RTC.Time(0,0), null)
    def port = new OutPort(name, data, new RingBuffer(8))
    registerOutPort(name, port)
    _port[name] = port
    _data[name] = data
  }

  def onData(String name, def data) {
    if (data.data.find(/^\<\?xml/)) {
      def parser = new XmlSlurper(new SAXParser())
      def doc = parser.parseText(data.data)
      def scores = doc.children().collect {
        [it.@rank.text().toInteger(), it.@score.text().toFloat(), it.@text.text()]
      }
    }
  }
  
  class Listener extends ConnectorDataListener {
    def m_name
    def m_type
    def m_rtc

    public Listener(def name, def type, def rtc) {
      m_name = name
      m_type = type
      m_rtc = rtc
    }

    @Override
    public void operator(ConnectorInfo info, OutputStream data) {
      m_rtc.onData(m_name, data)
    }
  }
}

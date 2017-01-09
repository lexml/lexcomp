package br.gov.lexml.symbolicobject.impl

import junit.framework.TestCase
import junit.framework.Assert.assertEquals

class NumberRendererTest extends TestCase {

  def test_render_number_1_to_letter_a() {
    assertEquals(NumberRenderer.alfa(1), "a")
  }

  def test_render_number_26_to_letter_z() {
    assertEquals(NumberRenderer.alfa(26), "z")
  }

  def test_render_number_27_to_letter_aa() {
    assertEquals(NumberRenderer.alfa(27), "aa")
  }

  def test_render_number_0_to_empty_string() {
    assertEquals(NumberRenderer.alfa(0), "")
  }
}

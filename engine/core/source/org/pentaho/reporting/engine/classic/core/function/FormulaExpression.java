/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001 - 2009 Object Refinery Ltd, Pentaho Corporation and Contributors..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.formula.Formula;
import org.pentaho.reporting.libraries.formula.FormulaContext;

/**
 * The formula expression is used to evaluate a LibFormula/OpenFormula expression. There is generally no need to
 * reference this class directly, as this expression is used automatically if a formula is specified in a element,
 * style-expression or common expression.
 *
 * @author Thomas Morgner
 */
public final class FormulaExpression extends AbstractExpression
{
  private static final Log logger = LogFactory.getLog(FormulaExpression.class);

  /**
   * A cached version of the compiled formula.
   */
  private transient Formula compiledFormula;
  /**
   * The formula namespace as defined by OpenFormula.
   */
  private String formulaNamespace;
  /**
   * The formula itself.
   */
  private String formulaExpression;
  /**
   * The formula as specified by the user. This is the formula and the namespace.
   */
  private String formula;
  /**
   * A flag indicating that the formula cannot be parsed.
   */
  private Exception formulaError;

  /**
   * Default Constructor.
   */
  public FormulaExpression()
  {
  }

  /**
   * Returns the defined formula context from the report processing context.
   *
   * @return the formula context.
   */
  private FormulaContext getFormulaContext()
  {
    final ProcessingContext globalContext = getRuntime().getProcessingContext();
    return globalContext.getFormulaContext();
  }

  /**
   * Returns the formula (incuding the optional namespace) as defined by the OpenFormula standard.
   *
   * @return the formula as text.
   */
  public String getFormula()
  {
    return formula;
  }

  /**
   * Returns the formula namespace. If the formula specified by the user starts with "=", then the namespace "report" is
   * assumed.
   *
   * @return the namespace of the formula.
   */
  public String getFormulaNamespace()
  {
    return formulaNamespace;
  }

  /**
   * Returns the formula expression.
   *
   * @return the formula expression.
   */
  public String getFormulaExpression()
  {
    return formulaExpression;
  }

  /**
   * Defines the formula (incuding the optional namespace) as defined by the OpenFormula standard.
   *
   * @param formula the formula as text.
   */
  public void setFormula(String formula)
  {
    this.formula = formula;
    if (formula == null)
    {
      formulaNamespace = null;
      formulaExpression = null;
    }
    else
    {
      if (formula.endsWith(";"))
      {
        logger.warn("A formula with a trailing semicolon is not valid. Auto-correcting the formula.");
        formula = formula.substring(0, formula.length() - 1);
      }

      if (formula.length() > 0 && formula.charAt(0) == '=')
      {
        formulaNamespace = "report";
        formulaExpression = formula.substring(1);
      }
      else
      {
        final int separator = formula.indexOf(':');
        if (separator <= 0 || ((separator + 1) == formula.length()))
        {
          // error: invalid formula.
          formulaNamespace = null;
          formulaExpression = null;
        }
        else
        {
          formulaNamespace = formula.substring(0, separator);
          formulaExpression = formula.substring(separator + 1);
        }
      }
    }
    this.compiledFormula = null;
    this.formulaError = null;
  }

  /**
   * Computes the value of the formula by evaluating the formula against the current data-row.
   *
   * @return the computed value or null, if an error occured.
   */
  private Object computeRegularValue()
  {
    if (formulaError != null)
    {
      return null;
    }

    if (formulaExpression == null)
    {
      return null;
    }

    try
    {
      if (compiledFormula == null)
      {
        compiledFormula = new Formula(formulaExpression);
      }

      final ExpressionRuntime expressionRuntime = getRuntime();

      final ReportFormulaContext context =
          new ReportFormulaContext(getFormulaContext(), expressionRuntime);
      try
      {
        compiledFormula.initialize(context);
        return compiledFormula.evaluate();
      }
      finally
      {
        context.close();
      }
    }
    catch (Exception e)
    {
      formulaError = e;
      if (FormulaExpression.logger.isDebugEnabled())
      {
        final Configuration config = getReportConfiguration();
        if ("true".equals(config.getConfigProperty(
            "org.pentaho.reporting.engine.classic.core.function.LogFormulaFailureCause")))
        {
          FormulaExpression.logger.debug("Failed to compute the regular value [" + formulaExpression + ']', e);
        }
        else
        {
          FormulaExpression.logger.debug("Failed to compute the regular value [" + formulaExpression + ']');
        }
      }
      return null;
    }
  }

  /**
   * Return the computed value of the formula.
   *
   * @return the value of the function.
   */
  public Object getValue()
  {
    try
    {
      return computeRegularValue();
    }
    catch (Exception e)
    {
      return null;
    }
  }

  public boolean isFormulaError()
  {
    return formulaError != null;
  }

  public Exception getFormulaError()
  {
    return formulaError;
  }

  /**
   * Return a completly separated copy of this function. The copy does no longer share any changeable objects with the
   * original function.
   *
   * @return a copy of this function.
   */
  public Expression getInstance()
  {
    final FormulaExpression o = (FormulaExpression) super.getInstance();
    o.compiledFormula = null;
    return o;
  }
}

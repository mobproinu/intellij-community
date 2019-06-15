// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.codeInsight.template.postfix.templates;

import com.intellij.codeInsight.template.postfix.templates.editable.JavaEditablePostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.editable.JavaPostfixTemplateExpressionCondition;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiExpression;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

import static com.intellij.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils.*;

public class LambdaPostfixTemplate extends JavaEditablePostfixTemplate {

  private static final JavaPostfixTemplateExpressionCondition myCondition = new JavaPostfixTemplateExpressionCondition() {

    @NotNull
    @Override
    public String getPresentableName() {
      return "code block";
    }

    @NotNull
    @Override
    public String getId() {
      return "code block";
    }

    @Override
    public boolean value(@NotNull PsiExpression expression) {
      return true;
    }
  };

  public LambdaPostfixTemplate(@NotNull JavaPostfixTemplateProvider provider) {
    super("lambda", "() -> $EXPR$", "() -> expr",
          Collections.singleton(myCondition),
          LanguageLevel.JDK_1_8, true, provider);
  }

  @Override
  public boolean isBuiltin() {
    return true;
  }
}

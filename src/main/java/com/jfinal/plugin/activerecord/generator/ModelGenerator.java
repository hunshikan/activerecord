/**
 * Copyright (c) 2011-2017, James Zhan 詹波 (jfinal@126.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.plugin.activerecord.generator;

import com.jfinal.kit.Kv;
import com.jfinal.template.Engine;
import com.jfinal.template.source.ClassPathSourceFactory;
import com.jfinal.kit.StrKit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Model 生成器
 */
public class ModelGenerator {

  protected String template = "/com/jfinal/plugin/activerecord/generator/model_template.jf";

  protected String modelPackageName;
  protected String baseModelPackageName;
  protected String modelOutputDir;
  protected boolean generateDaoInModel = false;

  public ModelGenerator(String modelPackageName, String baseModelPackageName, String modelOutputDir) {
    if (StrKit.isBlank(modelPackageName)) {
      throw new IllegalArgumentException("modelPackageName can not be blank.");
    }
    if (modelPackageName.contains("/") || modelPackageName.contains("\\")) {
      throw new IllegalArgumentException("modelPackageName error : " + modelPackageName);
    }
    if (StrKit.isBlank(baseModelPackageName)) {
      throw new IllegalArgumentException("baseModelPackageName can not be blank.");
    }
    if (baseModelPackageName.contains("/") || baseModelPackageName.contains("\\")) {
      throw new IllegalArgumentException("baseModelPackageName error : " + baseModelPackageName);
    }
    if (StrKit.isBlank(modelOutputDir)) {
      throw new IllegalArgumentException("modelOutputDir can not be blank.");
    }

    this.modelPackageName = modelPackageName;
    this.baseModelPackageName = baseModelPackageName;
    this.modelOutputDir = modelOutputDir;
  }

  /**
   * 使用自定义模板生成 model
   */
  public void setTemplate(String template) {
    this.template = template;
  }

  public void setGenerateDaoInModel(boolean generateDaoInModel) {
    this.generateDaoInModel = generateDaoInModel;
  }

  public void generate(List<TableMeta> tableMetas) {
    System.out.println("Generate model ...");
    System.out.println("Model Output Dir: " + modelOutputDir);

    Engine engine = Engine.create("forModel");
    engine.setSourceFactory(new ClassPathSourceFactory());
    engine.addSharedMethod(new StrKit());

    for (TableMeta tableMeta : tableMetas) {
      genModelContent(tableMeta);
    }
    writeToFile(tableMetas);
  }

  protected void genModelContent(TableMeta tableMeta) {
    Kv data = Kv.by("modelPackageName", modelPackageName);
    data.set("baseModelPackageName", baseModelPackageName);
    data.set("generateDaoInModel", generateDaoInModel);
    data.set("tableMeta", tableMeta);

    String ret = Engine.use("forModel").getTemplate(template).renderToString(data);
    tableMeta.modelContent = ret;
  }

  protected void writeToFile(List<TableMeta> tableMetas) {
    try {
      for (TableMeta tableMeta : tableMetas) {
        writeToFile(tableMeta);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 若 model 文件存在，则不生成，以免覆盖用户手写的代码
   */
  protected void writeToFile(TableMeta tableMeta) throws IOException {
    File dir = new File(modelOutputDir);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    String target = modelOutputDir + File.separator + tableMeta.modelName + ".java";

    File file = new File(target);
    if (file.exists()) {
      return;  // 若 Model 存在，不覆盖
    }

    FileWriter fw = new FileWriter(file);
    try {
      fw.write(tableMeta.modelContent);
    } finally {
      fw.close();
    }
  }
}



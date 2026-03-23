---
title: MLlib
description: "MLlib mechanics and machine learning pipeline design"
weight: 9
lastmod: "2026-01-07"
---

# MLlib (Machine Learning Library)

MLlib is Spark's distributed machine learning library. It enables training and prediction of machine learning models on large-scale datasets.

## MLlib Overview

### Two APIs

| API | Package | Data Type | Status |
|-----|---------|-----------|--------|
| **spark.ml** | `org.apache.spark.ml` | DataFrame | Currently recommended |
| spark.mllib | `org.apache.spark.mllib` | RDD | Maintenance mode |

This guide covers the **spark.ml** (DataFrame-based) API.

### Key Components

- **Transformer**: Data transformation (transform without fit)
- **Estimator**: Trainable model (fit creates Transformer)
- **Pipeline**: Connect multiple stages
- **Evaluator**: Evaluate model performance
- **CrossValidator/TrainValidationSplit**: Hyperparameter tuning

## Basic Workflow

```java
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.feature.VectorAssembler;

SparkSession spark = SparkSession.builder()
        .appName("MLlib Example")
        .master("local[*]")
        .getOrCreate();

// 1. Load data
Dataset<Row> data = spark.read()
        .option("header", "true")
        .option("inferSchema", "true")
        .csv("data.csv");

// 2. Create feature vector
VectorAssembler assembler = new VectorAssembler()
        .setInputCols(new String[]{"feature1", "feature2", "feature3"})
        .setOutputCol("features");

Dataset<Row> assembled = assembler.transform(data);

// 3. Train/test split
Dataset<Row>[] splits = assembled.randomSplit(new double[]{0.8, 0.2}, 42);
Dataset<Row> training = splits[0];
Dataset<Row> test = splits[1];

// 4. Train model
LogisticRegression lr = new LogisticRegression()
        .setFeaturesCol("features")
        .setLabelCol("label")
        .setMaxIter(100)
        .setRegParam(0.1);

LogisticRegressionModel model = lr.fit(training);

// 5. Predict
Dataset<Row> predictions = model.transform(test);
predictions.select("label", "prediction", "probability").show(10);

// 6. Save model
model.write().overwrite().save("models/logistic-regression");

// 7. Load model
LogisticRegressionModel loadedModel = LogisticRegressionModel.load("models/logistic-regression");
```

## Feature Transformation

### VectorAssembler

Combine multiple columns into a single feature vector:

```java
VectorAssembler assembler = new VectorAssembler()
        .setInputCols(new String[]{"age", "income", "score"})
        .setOutputCol("features")
        .setHandleInvalid("skip");  // skip, keep, error

Dataset<Row> assembled = assembler.transform(data);
```

### StringIndexer

Convert strings to numeric indices:

```java
StringIndexer indexer = new StringIndexer()
        .setInputCol("category")
        .setOutputCol("categoryIndex")
        .setHandleInvalid("keep");  // Handle unseen values

StringIndexerModel indexerModel = indexer.fit(data);
Dataset<Row> indexed = indexerModel.transform(data);
```

### OneHotEncoder

One-hot encode categorical variables:

```java
OneHotEncoder encoder = new OneHotEncoder()
        .setInputCols(new String[]{"categoryIndex"})
        .setOutputCols(new String[]{"categoryVec"});

Dataset<Row> encoded = encoder.fit(indexed).transform(indexed);
```

### StandardScaler

Normalize features:

```java
StandardScaler scaler = new StandardScaler()
        .setInputCol("features")
        .setOutputCol("scaledFeatures")
        .setWithStd(true)
        .setWithMean(true);

StandardScalerModel scalerModel = scaler.fit(data);
Dataset<Row> scaled = scalerModel.transform(data);
```

### Tokenizer / HashingTF

Text processing:

```java
// Tokenization
Tokenizer tokenizer = new Tokenizer()
        .setInputCol("text")
        .setOutputCol("words");

// TF (Term Frequency)
HashingTF hashingTF = new HashingTF()
        .setInputCol("words")
        .setOutputCol("features")
        .setNumFeatures(10000);

// IDF (Inverse Document Frequency)
IDF idf = new IDF()
        .setInputCol("features")
        .setOutputCol("tfidf");
```

## Pipeline

Connect multiple stages into a single workflow:

```java
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;

// Define stages
StringIndexer labelIndexer = new StringIndexer()
        .setInputCol("label")
        .setOutputCol("indexedLabel");

VectorAssembler assembler = new VectorAssembler()
        .setInputCols(new String[]{"feature1", "feature2"})
        .setOutputCol("features");

StandardScaler scaler = new StandardScaler()
        .setInputCol("features")
        .setOutputCol("scaledFeatures");

LogisticRegression lr = new LogisticRegression()
        .setFeaturesCol("scaledFeatures")
        .setLabelCol("indexedLabel");

// Create pipeline
Pipeline pipeline = new Pipeline()
        .setStages(new PipelineStage[]{
            labelIndexer,
            assembler,
            scaler,
            lr
        });

// Train (entire pipeline)
PipelineModel model = pipeline.fit(training);

// Predict
Dataset<Row> predictions = model.transform(test);

// Save/Load
model.write().overwrite().save("models/pipeline");
PipelineModel loadedModel = PipelineModel.load("models/pipeline");
```

## Classification

### Logistic Regression

```java
LogisticRegression lr = new LogisticRegression()
        .setMaxIter(100)
        .setRegParam(0.1)
        .setElasticNetParam(0.8)  // L1/L2 ratio
        .setFamily("multinomial");  // Multi-class

LogisticRegressionModel model = lr.fit(training);

// Model info
System.out.println("Coefficients: " + model.coefficientMatrix());
System.out.println("Intercept: " + model.interceptVector());
```

### Decision Tree

```java
DecisionTreeClassifier dt = new DecisionTreeClassifier()
        .setLabelCol("label")
        .setFeaturesCol("features")
        .setMaxDepth(10)
        .setMinInstancesPerNode(5)
        .setImpurity("gini");  // gini, entropy

DecisionTreeClassificationModel model = dt.fit(training);

// Feature importance
System.out.println("Feature Importances: " + model.featureImportances());
```

### Random Forest

```java
RandomForestClassifier rf = new RandomForestClassifier()
        .setLabelCol("label")
        .setFeaturesCol("features")
        .setNumTrees(100)
        .setMaxDepth(10)
        .setFeatureSubsetStrategy("sqrt");

RandomForestClassificationModel model = rf.fit(training);
```

### Gradient Boosted Trees

```java
GBTClassifier gbt = new GBTClassifier()
        .setLabelCol("label")
        .setFeaturesCol("features")
        .setMaxIter(50)
        .setMaxDepth(5)
        .setStepSize(0.1);

GBTClassificationModel model = gbt.fit(training);
```

## Regression

### Linear Regression

```java
LinearRegression lr = new LinearRegression()
        .setMaxIter(100)
        .setRegParam(0.1)
        .setElasticNetParam(0.5);

LinearRegressionModel model = lr.fit(training);

// Training summary
LinearRegressionTrainingSummary summary = model.summary();
System.out.println("RMSE: " + summary.rootMeanSquaredError());
System.out.println("R2: " + summary.r2());
```

### Random Forest Regression

```java
RandomForestRegressor rf = new RandomForestRegressor()
        .setLabelCol("label")
        .setFeaturesCol("features")
        .setNumTrees(100);

RandomForestRegressionModel model = rf.fit(training);
```

## Clustering

### K-Means

```java
KMeans kmeans = new KMeans()
        .setK(5)
        .setSeed(42)
        .setMaxIter(100)
        .setFeaturesCol("features");

KMeansModel model = kmeans.fit(data);

// Cluster centers
Vector[] centers = model.clusterCenters();
for (int i = 0; i < centers.length; i++) {
    System.out.println("Cluster " + i + ": " + centers[i]);
}

// Cluster assignments
Dataset<Row> predictions = model.transform(data);

// Cost (WSSSE)
double cost = model.summary().trainingCost();
```

### Anomaly Detection (Isolation Forest Alternative)

```java
// Spark MLlib doesn't have Isolation Forest
// Alternative: K-Means based distance calculation
Dataset<Row> withDistance = predictions
    .withColumn("distanceToCenter", calculateDistance(col("features"), col("prediction")));

Dataset<Row> anomalies = withDistance
    .filter(col("distanceToCenter").gt(threshold));
```

## Model Evaluation

### Classification Evaluation

```java
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;

// Multi-class
MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
        .setLabelCol("label")
        .setPredictionCol("prediction")
        .setMetricName("accuracy");  // accuracy, f1, weightedPrecision, weightedRecall

double accuracy = evaluator.evaluate(predictions);
System.out.println("Accuracy: " + accuracy);

// Binary classification (AUC)
BinaryClassificationEvaluator binEvaluator = new BinaryClassificationEvaluator()
        .setLabelCol("label")
        .setRawPredictionCol("rawPrediction")
        .setMetricName("areaUnderROC");

double auc = binEvaluator.evaluate(predictions);
System.out.println("AUC: " + auc);
```

### Regression Evaluation

```java
import org.apache.spark.ml.evaluation.RegressionEvaluator;

RegressionEvaluator evaluator = new RegressionEvaluator()
        .setLabelCol("label")
        .setPredictionCol("prediction")
        .setMetricName("rmse");  // rmse, mse, r2, mae

double rmse = evaluator.evaluate(predictions);
System.out.println("RMSE: " + rmse);
```

## Hyperparameter Tuning

### CrossValidator

```java
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.ml.tuning.ParamGridBuilder;

// Parameter grid
ParamMap[] paramGrid = new ParamGridBuilder()
        .addGrid(lr.regParam(), new double[]{0.1, 0.01})
        .addGrid(lr.elasticNetParam(), new double[]{0.0, 0.5, 1.0})
        .addGrid(lr.maxIter(), new int[]{50, 100})
        .build();

// Cross validation
CrossValidator cv = new CrossValidator()
        .setEstimator(pipeline)
        .setEvaluator(new BinaryClassificationEvaluator())
        .setEstimatorParamMaps(paramGrid)
        .setNumFolds(5)
        .setParallelism(4);  // Parallel execution

CrossValidatorModel cvModel = cv.fit(training);

// Best model
PipelineModel bestModel = (PipelineModel) cvModel.bestModel();

// Scores for each parameter combination
double[] avgMetrics = cvModel.avgMetrics();
```

### TrainValidationSplit

A faster alternative to cross-validation:

```java
import org.apache.spark.ml.tuning.TrainValidationSplit;
import org.apache.spark.ml.tuning.TrainValidationSplitModel;

TrainValidationSplit tvs = new TrainValidationSplit()
        .setEstimator(pipeline)
        .setEvaluator(new BinaryClassificationEvaluator())
        .setEstimatorParamMaps(paramGrid)
        .setTrainRatio(0.8);  // 80% train, 20% validation

TrainValidationSplitModel tvsModel = tvs.fit(training);
```

## Practical Example: Customer Churn Prediction

```java
public class ChurnPrediction {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("Churn Prediction")
                .master("local[*]")
                .getOrCreate();

        // Load data
        Dataset<Row> data = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv("customer_data.csv");

        // Label indexing
        StringIndexer labelIndexer = new StringIndexer()
                .setInputCol("churned")
                .setOutputCol("label");

        // Categorical column encoding
        String[] categoricalCols = {"gender", "region", "plan_type"};
        StringIndexer[] indexers = new StringIndexer[categoricalCols.length];
        OneHotEncoder[] encoders = new OneHotEncoder[categoricalCols.length];

        for (int i = 0; i < categoricalCols.length; i++) {
            indexers[i] = new StringIndexer()
                    .setInputCol(categoricalCols[i])
                    .setOutputCol(categoricalCols[i] + "_idx");
            encoders[i] = new OneHotEncoder()
                    .setInputCols(new String[]{categoricalCols[i] + "_idx"})
                    .setOutputCols(new String[]{categoricalCols[i] + "_vec"});
        }

        // Combine numeric + encoded columns
        String[] numericCols = {"age", "tenure", "monthly_charges", "total_charges"};
        String[] encodedCols = {"gender_vec", "region_vec", "plan_type_vec"};
        String[] allFeatureCols = Stream.concat(
                Arrays.stream(numericCols),
                Arrays.stream(encodedCols)
        ).toArray(String[]::new);

        VectorAssembler assembler = new VectorAssembler()
                .setInputCols(allFeatureCols)
                .setOutputCol("rawFeatures")
                .setHandleInvalid("skip");

        // Normalization
        StandardScaler scaler = new StandardScaler()
                .setInputCol("rawFeatures")
                .setOutputCol("features")
                .setWithStd(true)
                .setWithMean(true);

        // Classifier
        RandomForestClassifier rf = new RandomForestClassifier()
                .setLabelCol("label")
                .setFeaturesCol("features")
                .setNumTrees(100);

        // Configure pipeline
        List<PipelineStage> stages = new ArrayList<>();
        stages.add(labelIndexer);
        stages.addAll(Arrays.asList(indexers));
        stages.addAll(Arrays.asList(encoders));
        stages.add(assembler);
        stages.add(scaler);
        stages.add(rf);

        Pipeline pipeline = new Pipeline()
                .setStages(stages.toArray(new PipelineStage[0]));

        // Train/test split
        Dataset<Row>[] splits = data.randomSplit(new double[]{0.8, 0.2}, 42);
        Dataset<Row> training = splits[0];
        Dataset<Row> test = splits[1];

        // Hyperparameter tuning
        ParamMap[] paramGrid = new ParamGridBuilder()
                .addGrid(rf.numTrees(), new int[]{50, 100, 150})
                .addGrid(rf.maxDepth(), new int[]{5, 10, 15})
                .build();

        CrossValidator cv = new CrossValidator()
                .setEstimator(pipeline)
                .setEvaluator(new BinaryClassificationEvaluator())
                .setEstimatorParamMaps(paramGrid)
                .setNumFolds(3)
                .setParallelism(4);

        // Train
        CrossValidatorModel cvModel = cv.fit(training);

        // Evaluate
        Dataset<Row> predictions = cvModel.transform(test);

        BinaryClassificationEvaluator evaluator = new BinaryClassificationEvaluator();
        double auc = evaluator.evaluate(predictions);
        System.out.println("Test AUC: " + auc);

        // Save model
        cvModel.write().overwrite().save("models/churn-prediction");

        spark.stop();
    }
}
```

## Next Steps

- [Performance Tuning](tuning/) - ML workload optimization
- [Deployment](deployment/) - Model serving and batch prediction

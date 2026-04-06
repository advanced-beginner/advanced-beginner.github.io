---
title: MLlib
description: "MLlib의 작동 원리와 머신러닝 파이프라인 구성을 설명합니다."
weight: 9
lastmod: "2026-01-15"
author:
  name: Advanced Beginner
  github: advanced-beginner
---

{{< callout type="info" title="TL;DR" >}}
- MLlib은 DataFrame 기반 분산 머신러닝 라이브러리 (spark.ml 패키지)
- Pipeline으로 전처리 → 학습 → 예측 단계를 연결
- Transformer(변환), Estimator(학습), Evaluator(평가) 패턴
- CrossValidator/TrainValidationSplit로 하이퍼파라미터 튜닝
{{< /callout >}}

**대상 독자**: 대규모 데이터에서 머신러닝을 수행하려는 ML 엔지니어

**선수 지식**:
- [DataFrame과 Dataset](dataframe-dataset/) API
- 머신러닝 기본 개념 (분류, 회귀, 클러스터링)
- 특성 공학(Feature Engineering) 기초

**소요 시간**: 약 25-30분

{{< callout type="info" title="ML 기초 5분 요약 — Java 개발자를 위한 최소 배경지식" >}}
머신러닝(ML)이 처음이라면 아래 핵심 개념을 먼저 이해하세요:

- **머신러닝이란?** 데이터(입력)를 모델에 넣으면 예측(출력)을 반환하는 구조입니다. 규칙을 사람이 작성하는 대신, 데이터에서 규칙을 자동으로 발견합니다.
- **특성(Feature)이란?** 모델에 입력되는 개별 데이터 항목입니다. 예: 고객의 나이, 월 결제액, 가입 기간 등. Java 객체의 필드와 유사합니다.
- **학습(Training)이란?** 과거 데이터로 모델의 규칙(파라미터)을 자동으로 찾아가는 과정입니다. `estimator.fit(data)`가 이에 해당합니다.
- **분류 vs 회귀**: 결과가 범주(예/아니오)이면 분류, 숫자(가격, 점수)이면 회귀입니다.

더 깊이 학습하려면:
- [Google ML Crash Course](https://developers.google.com/machine-learning/crash-course) (무료, 한글 자막)
- [scikit-learn 튜토리얼](https://scikit-learn.org/stable/tutorial/) (Python이지만 ML 개념 이해에 유용)
- [StatQuest YouTube 채널](https://www.youtube.com/c/joshstarmer) (시각적 설명)
{{< /callout >}}

---

MLlib은 Spark의 분산 머신러닝 라이브러리입니다. 대규모 데이터셋에서 머신러닝 모델을 학습하고 예측할 수 있습니다.

## 비유로 이해하는 MLlib

| 개념 | 비유 | 핵심 아이디어 |
|------|------|---------------|
| **Transformer** | 재료 손질사 | 원재료(데이터)를 조리 가능한 형태로 변환. 학습 없이 규칙대로 처리 |
| **Estimator** | 요리사 | 레시피(알고리즘)로 연습(학습)하여 실력(모델)을 갖춤 |
| **Model** | 숙련된 요리사 | 학습 완료된 상태. 새 재료가 와도 바로 요리(예측) 가능 |
| **Pipeline** | 조리 라인 | 재료 손질 → 양념 → 조리 → 플레이팅을 하나로 연결 |
| **Evaluator** | 음식 평론가 | 완성된 요리(모델)의 품질(정확도)을 평가 |
| **CrossValidator** | 맛 테스트 반복 | 여러 번 맛보고 가장 좋은 레시피(하이퍼파라미터) 선택 |
| **Feature Vector** | 표준화된 재료 목록 | 모든 재료를 동일한 형식(벡터)으로 정리 |

> **핵심 원리**: MLlib은 "전처리 → 학습 → 예측 → 평가"의 반복 가능한 파이프라인을 구축하여, 대규모 데이터에서도 일관된 ML 워크플로우를 제공합니다.

## 왜 분산 ML이 필요한가? (설계 철학)

**질문: scikit-learn이 있는데 왜 MLlib을 써야 하나요?**

| 상황 | scikit-learn | MLlib |
|------|--------------|-------|
| 데이터 크기 | 메모리에 맞는 크기 | 메모리를 초과하는 대용량 |
| 학습 시간 | 단일 머신 성능에 의존 | 클러스터로 병렬 학습 |
| 데이터 위치 | 로컬 파일 | HDFS, S3 등 분산 저장소 |
| 통합성 | 별도 ETL 필요 | Spark 파이프라인과 통합 |

**MLlib의 핵심 설계 원칙**

1. **DataFrame 기반 통합**: 전처리(Spark SQL) → 학습(MLlib) → 저장(Parquet)이 하나의 파이프라인
2. **파이프라인 재사용**: 학습된 파이프라인을 저장하고 프로덕션에서 그대로 사용
3. **하이퍼파라미터 병렬 탐색**: CrossValidator가 여러 조합을 클러스터에서 동시에 실험

## MLlib 개요

**두 가지 API**

| API | 패키지 | 데이터 타입 | 상태 |
|-----|--------|-----------|------|
| **spark.ml** | `org.apache.spark.ml` | DataFrame | 현재 권장 |
| spark.mllib | `org.apache.spark.mllib` | RDD | 유지보수 모드 |

이 가이드는 **spark.ml** (DataFrame 기반) API를 다룹니다.

**주요 구성요소**

- **Transformer**: 데이터 변환 (fit 없이 transform)
- **Estimator**: 학습 가능한 모델 (fit으로 Transformer 생성)
- **Pipeline**: 여러 단계를 연결
- **Evaluator**: 모델 성능 평가
- **CrossValidator/TrainValidationSplit**: 하이퍼파라미터 튜닝

{{< callout type="info" title="핵심 포인트" >}}
- **spark.ml** 패키지 사용 (DataFrame 기반, 권장)
- **spark.mllib**은 RDD 기반으로 유지보수 모드
- Transformer: 입력 DataFrame → 출력 DataFrame 변환
- Estimator: fit()으로 학습하여 Transformer 생성
{{< /callout >}}

## 기본 워크플로우

```java
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.feature.VectorAssembler;

SparkSession spark = SparkSession.builder()
        .appName("MLlib Example")
        .master("local[*]")
        .getOrCreate();

// 1. 데이터 로드
Dataset<Row> data = spark.read()
        .option("header", "true")
        .option("inferSchema", "true")
        .csv("data.csv");

// 2. 특성 벡터 생성
VectorAssembler assembler = new VectorAssembler()
        .setInputCols(new String[]{"feature1", "feature2", "feature3"})
        .setOutputCol("features");

Dataset<Row> assembled = assembler.transform(data);

// 3. 학습/테스트 분할
Dataset<Row>[] splits = assembled.randomSplit(new double[]{0.8, 0.2}, 42);
Dataset<Row> training = splits[0];
Dataset<Row> test = splits[1];

// 4. 모델 학습
LogisticRegression lr = new LogisticRegression()
        .setFeaturesCol("features")
        .setLabelCol("label")
        .setMaxIter(100)
        .setRegParam(0.1);

LogisticRegressionModel model = lr.fit(training);

// 5. 예측
Dataset<Row> predictions = model.transform(test);
predictions.select("label", "prediction", "probability").show(10);

// 6. 모델 저장
model.write().overwrite().save("models/logistic-regression");

// 7. 모델 로드
LogisticRegressionModel loadedModel = LogisticRegressionModel.load("models/logistic-regression");
```

## 특성 변환 (Feature Transformation)

**VectorAssembler**

ML 모델은 숫자 배열(벡터)만 입력받을 수 있습니다. VectorAssembler는 여러 컬럼(나이, 소득, 점수 등)을 하나의 숫자 배열로 합쳐서 모델이 이해할 수 있는 형태로 만듭니다:

```java
VectorAssembler assembler = new VectorAssembler()
        .setInputCols(new String[]{"age", "income", "score"})
        .setOutputCol("features")
        .setHandleInvalid("skip");  // skip, keep, error

Dataset<Row> assembled = assembler.transform(data);
```

**StringIndexer**

문자열을 숫자 인덱스로 변환:

```java
StringIndexer indexer = new StringIndexer()
        .setInputCol("category")
        .setOutputCol("categoryIndex")
        .setHandleInvalid("keep");  // unseen 값 처리

StringIndexerModel indexerModel = indexer.fit(data);
Dataset<Row> indexed = indexerModel.transform(data);
```

**OneHotEncoder**

범주형 변수를 원-핫 인코딩:

```java
OneHotEncoder encoder = new OneHotEncoder()
        .setInputCols(new String[]{"categoryIndex"})
        .setOutputCols(new String[]{"categoryVec"});

Dataset<Row> encoded = encoder.fit(indexed).transform(indexed);
```

**StandardScaler**

특성 정규화:

```java
StandardScaler scaler = new StandardScaler()
        .setInputCol("features")
        .setOutputCol("scaledFeatures")
        .setWithStd(true)
        .setWithMean(true);

StandardScalerModel scalerModel = scaler.fit(data);
Dataset<Row> scaled = scalerModel.transform(data);
```

**Tokenizer / HashingTF**

텍스트 처리:

```java
// 토큰화
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

여러 단계를 하나의 워크플로우로 연결:

```java
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;

// 단계 정의
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

// 파이프라인 생성
Pipeline pipeline = new Pipeline()
        .setStages(new PipelineStage[]{
            labelIndexer,
            assembler,
            scaler,
            lr
        });

// 학습 (전체 파이프라인)
PipelineModel model = pipeline.fit(training);

// 예측
Dataset<Row> predictions = model.transform(test);

// 저장/로드
model.write().overwrite().save("models/pipeline");
PipelineModel loadedModel = PipelineModel.load("models/pipeline");
```

## 분류 (Classification)

**로지스틱 회귀**

```java
LogisticRegression lr = new LogisticRegression()
        .setMaxIter(100)
        .setRegParam(0.1)            // 규제 강도: 과적합 방지 (값이 클수록 규제 강함)
        .setElasticNetParam(0.8)     // L1/L2 비율: 0=L2(Ridge), 1=L1(Lasso), 중간=혼합
        .setFamily("multinomial");   // 다중 클래스

LogisticRegressionModel model = lr.fit(training);

// 모델 정보
System.out.println("Coefficients: " + model.coefficientMatrix());
System.out.println("Intercept: " + model.interceptVector());
```

**결정 트리**

```java
DecisionTreeClassifier dt = new DecisionTreeClassifier()
        .setLabelCol("label")
        .setFeaturesCol("features")
        .setMaxDepth(10)
        .setMinInstancesPerNode(5)
        .setImpurity("gini");  // gini, entropy

DecisionTreeClassificationModel model = dt.fit(training);

// 특성 중요도
System.out.println("Feature Importances: " + model.featureImportances());
```

**랜덤 포레스트**

```java
RandomForestClassifier rf = new RandomForestClassifier()
        .setLabelCol("label")
        .setFeaturesCol("features")
        .setNumTrees(100)
        .setMaxDepth(10)
        .setFeatureSubsetStrategy("sqrt");

RandomForestClassificationModel model = rf.fit(training);
```

**Gradient Boosted Trees**

```java
GBTClassifier gbt = new GBTClassifier()
        .setLabelCol("label")
        .setFeaturesCol("features")
        .setMaxIter(50)
        .setMaxDepth(5)
        .setStepSize(0.1);

GBTClassificationModel model = gbt.fit(training);
```

## 회귀 (Regression)

**선형 회귀**

```java
LinearRegression lr = new LinearRegression()
        .setMaxIter(100)
        .setRegParam(0.1)            // 규제 강도: 과적합 방지 (0이면 규제 없음)
        .setElasticNetParam(0.5);    // L1/L2 비율: 0=Ridge, 1=Lasso, 0.5=절반씩 혼합

LinearRegressionModel model = lr.fit(training);

// 학습 요약
LinearRegressionTrainingSummary summary = model.summary();
System.out.println("RMSE: " + summary.rootMeanSquaredError());
System.out.println("R2: " + summary.r2());
```

**랜덤 포레스트 회귀**

```java
RandomForestRegressor rf = new RandomForestRegressor()
        .setLabelCol("label")
        .setFeaturesCol("features")
        .setNumTrees(100);

RandomForestRegressionModel model = rf.fit(training);
```

## 클러스터링 (Clustering)

**K-Means**

```java
KMeans kmeans = new KMeans()
        .setK(5)
        .setSeed(42)
        .setMaxIter(100)
        .setFeaturesCol("features");

KMeansModel model = kmeans.fit(data);

// 클러스터 센터
Vector[] centers = model.clusterCenters();
for (int i = 0; i < centers.length; i++) {
    System.out.println("Cluster " + i + ": " + centers[i]);
}

// 클러스터 할당
Dataset<Row> predictions = model.transform(data);

// 비용 (WSSSE)
double cost = model.summary().trainingCost();
```

**이상 탐지 (Isolation Forest 대안)**

```java
// Spark MLlib에는 Isolation Forest가 없음
// 대안: K-Means 기반 거리 계산

// calculateDistance는 직접 구현이 필요합니다.
// 아래는 UDF(사용자 정의 함수)로 유클리드 거리를 계산하는 예시입니다:
UserDefinedFunction distanceUdf = udf((Vector features, int clusterId) -> {
    Vector center = model.clusterCenters()[clusterId];
    double sum = 0.0;
    for (int i = 0; i < features.size(); i++) {
        double diff = features.apply(i) - center.apply(i);
        sum += diff * diff;
    }
    return Math.sqrt(sum);
}, DataTypes.DoubleType);

Dataset<Row> withDistance = predictions
    .withColumn("distanceToCenter", distanceUdf.apply(col("features"), col("prediction")));

double threshold = 10.0;  // 비즈니스 요구에 맞게 조정
Dataset<Row> anomalies = withDistance
    .filter(col("distanceToCenter").gt(threshold));
```

## 모델 평가

**분류 평가**

```java
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;

// 다중 클래스
MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
        .setLabelCol("label")
        .setPredictionCol("prediction")
        .setMetricName("accuracy");  // accuracy, f1, weightedPrecision, weightedRecall

double accuracy = evaluator.evaluate(predictions);
System.out.println("Accuracy: " + accuracy);

// 이진 분류 (AUC)
BinaryClassificationEvaluator binEvaluator = new BinaryClassificationEvaluator()
        .setLabelCol("label")
        .setRawPredictionCol("rawPrediction")
        .setMetricName("areaUnderROC");

double auc = binEvaluator.evaluate(predictions);
System.out.println("AUC: " + auc);
```

**회귀 평가**

```java
import org.apache.spark.ml.evaluation.RegressionEvaluator;

RegressionEvaluator evaluator = new RegressionEvaluator()
        .setLabelCol("label")
        .setPredictionCol("prediction")
        .setMetricName("rmse");  // rmse, mse, r2, mae

double rmse = evaluator.evaluate(predictions);
System.out.println("RMSE: " + rmse);
```

## 하이퍼파라미터 튜닝

**CrossValidator**

```java
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.ml.tuning.ParamGridBuilder;

// 파라미터 그리드
ParamMap[] paramGrid = new ParamGridBuilder()
        .addGrid(lr.regParam(), new double[]{0.1, 0.01})
        .addGrid(lr.elasticNetParam(), new double[]{0.0, 0.5, 1.0})
        .addGrid(lr.maxIter(), new int[]{50, 100})
        .build();

// 교차 검증
CrossValidator cv = new CrossValidator()
        .setEstimator(pipeline)
        .setEvaluator(new BinaryClassificationEvaluator())
        .setEstimatorParamMaps(paramGrid)
        .setNumFolds(5)
        .setParallelism(4);  // 병렬 실행

CrossValidatorModel cvModel = cv.fit(training);

// 최적 모델
PipelineModel bestModel = (PipelineModel) cvModel.bestModel();

// 각 파라미터 조합의 점수
double[] avgMetrics = cvModel.avgMetrics();
```

**TrainValidationSplit**

교차 검증보다 빠른 대안:

```java
import org.apache.spark.ml.tuning.TrainValidationSplit;
import org.apache.spark.ml.tuning.TrainValidationSplitModel;

TrainValidationSplit tvs = new TrainValidationSplit()
        .setEstimator(pipeline)
        .setEvaluator(new BinaryClassificationEvaluator())
        .setEstimatorParamMaps(paramGrid)
        .setTrainRatio(0.8);  // 80% 학습, 20% 검증

TrainValidationSplitModel tvsModel = tvs.fit(training);
```

## 실전 예제: 고객 이탈 예측

{{< callout type="info" title="전체 파이프라인 한눈에 보기" >}}
1. **데이터 로드**: CSV에서 고객 데이터 읽기
2. **레이블 인덱싱**: "churned" 문자열을 숫자(0/1)로 변환
3. **범주형 인코딩**: gender, region, plan_type → 숫자 인덱스 → 원-핫 벡터
4. **특성 결합**: 수치형 컬럼 + 인코딩된 컬럼 → 하나의 벡터
5. **정규화**: 특성 값 범위를 통일 (StandardScaler)
6. **학습**: RandomForest로 이탈 패턴 학습
7. **튜닝**: CrossValidator로 최적 하이퍼파라미터 탐색
8. **평가**: AUC로 모델 성능 측정
{{< /callout >}}

```java
public class ChurnPrediction {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("Churn Prediction")
                .master("local[*]")
                .getOrCreate();

        // Step 1: 데이터 로드
        Dataset<Row> data = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv("customer_data.csv");

        // Step 2: 레이블 인덱싱 — "Yes"/"No"를 1/0 숫자로 변환
        StringIndexer labelIndexer = new StringIndexer()
                .setInputCol("churned")
                .setOutputCol("label");

        // Step 3: 범주형 컬럼 인코딩 — 문자열을 ML 모델이 이해할 수 있는 숫자로 변환
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

        // Step 4: 수치형 + 인코딩된 컬럼을 하나의 벡터로 결합
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

        // Step 5: 정규화 — 나이(20~80)와 요금(10~300) 등 범위가 다른 값을 동일 스케일로 변환
        StandardScaler scaler = new StandardScaler()
                .setInputCol("rawFeatures")
                .setOutputCol("features")
                .setWithStd(true)
                .setWithMean(true);

        // Step 6: 분류기 — 100개 결정 트리의 다수결로 이탈 여부 예측
        RandomForestClassifier rf = new RandomForestClassifier()
                .setLabelCol("label")
                .setFeaturesCol("features")
                .setNumTrees(100);

        // 파이프라인 구성
        List<PipelineStage> stages = new ArrayList<>();
        stages.add(labelIndexer);
        stages.addAll(Arrays.asList(indexers));
        stages.addAll(Arrays.asList(encoders));
        stages.add(assembler);
        stages.add(scaler);
        stages.add(rf);

        Pipeline pipeline = new Pipeline()
                .setStages(stages.toArray(new PipelineStage[0]));

        // 학습/테스트 분할
        Dataset<Row>[] splits = data.randomSplit(new double[]{0.8, 0.2}, 42);
        Dataset<Row> training = splits[0];
        Dataset<Row> test = splits[1];

        // Step 7: 하이퍼파라미터 튜닝 — 트리 개수, 깊이 조합을 3-fold 교차검증으로 실험
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

        // 학습
        CrossValidatorModel cvModel = cv.fit(training);

        // Step 8: 평가 — AUC(Area Under ROC Curve)로 모델 성능 측정 (1.0에 가까울수록 우수)
        Dataset<Row> predictions = cvModel.transform(test);

        BinaryClassificationEvaluator evaluator = new BinaryClassificationEvaluator();
        double auc = evaluator.evaluate(predictions);
        System.out.println("Test AUC: " + auc);

        // 모델 저장
        cvModel.write().overwrite().save("models/churn-prediction");

        spark.stop();
    }
}
```

## 다음 단계

- [성능 튜닝](tuning/) - ML 워크로드 최적화
- [배포](deployment/) - 모델 서빙과 배치 예측

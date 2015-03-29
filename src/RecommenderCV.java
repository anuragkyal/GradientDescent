/**
 * Created by anurag on 3/27/15.
 */
public class RecommenderCV {
    int folds = 10;
    int users = 1000;
    int items = 2069;
    int ratings = 87768;
    int testSize = ratings/folds;
    double[][] r;
    Recommender recommender;

    RecommenderCV(int ratings, int folds, double[][] r, Recommender recommender){
        this.folds = folds;
        this.ratings = ratings;
        testSize = ratings/folds;
        this.r = r;
        this.recommender = recommender;
    }

    public double performFold(int foldNumber){
        double[][] newR = new double[users][items];
        double[][] testSet = new double[testSize][3];

        for(int i=0; i<users; i++){
            for(int j=0; j<items; j++){
                newR[i][j] = r[i][j];
            }
        }

        for(int i=foldNumber*testSize; i<(foldNumber+1)*testSize; i++){
            int testUserId = recommender.readRatings.get(i)[0];
            int testItemId = recommender.readRatings.get(i)[1];

            testSet[i-foldNumber*testSize][0] = testUserId;
            testSet[i-foldNumber*testSize][1] = testItemId;
            testSet[i-foldNumber*testSize][2] = recommender.readRatings.get(i)[2];

            newR[testUserId][testItemId] = 0;
        }

        recommender.factorize(newR, 500, .05);
        return computeRMSE(testSet, recommender.result());
    }

    public double computeRMSE(double[][] testSet, double[][] result){
        double error = 0;

        for(int t=0; t<testSet.length; t++){
            int testUserId = (int) testSet[t][0];
            int testItemId = (int) testSet[t][1];
            double rating = testSet[t][2];

            error += Math.pow((rating - result[testUserId][testItemId]), 2);
        }

        return Math.sqrt(error/testSet.length);
    }

    public double performFolds(){
        double masterError = 0;
        for(int i=0; i<folds; i++){
            System.out.println("Fold:" + (i+1));

            double error = performFold(i);
            System.out.println("RMSE=" + error);
            masterError += error;
        }

        return masterError/folds;
    }

    public static void main(String[] args) throws Exception {
        /*int[] K = new int[]{1, 3, 5};
        double[] A = new double[]{0.0001, 0.0005, 0.001};
        double[] B = new double[]{0.05, 0.1, 0.5};

        for(int k: K) {
            for (double a : A) {
                for (double b : B) {
                    System.out.println("k=" + k + " a=" + a + " b=" + b);
                    */
                    Recommender recommender = new Recommender(Integer.parseInt(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]));
                    recommender.readRatings(args[0]);
                    RecommenderCV recommenderCV = new RecommenderCV(recommender.ratings, Integer.parseInt(args[4]), recommender.r, recommender);
                    System.out.println("Final RMSE=" + recommenderCV.performFolds());
                /*}
            }
        }*/
    }
}

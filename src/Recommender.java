import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by anurag on 3/27/15.
 */
public class Recommender {
    final int users = 1000;
    final int items = 2069;
    double[][] r = new double[users][items];
    List<int[]> readRatings = new ArrayList<int[]>();
    int ratings = 0;

    int K;
    double a;  //Learning rate
    double b;  //Regularization factor

    double[][] p;
    double[][] q;

    Recommender(int K, double a, double b){
        this.K = K;
        this.a = a;
        this.b = b;

        p = new double[users][K];
        q = new double[items][K];
    }

    public void readRatings(String inputPath) throws Exception {
        BufferedReader b = new BufferedReader(new FileReader(inputPath));

        String line = b.readLine();
        while(line != null){
            String[] parts = line.split(",");

            int userId = Integer.parseInt(parts[0]);
            int itemId = Integer.parseInt(parts[1]);
            int rating = Integer.parseInt(parts[2]);

            if(userId<1 || userId >users){
                throw new Exception("User Id is out of range");
            } else if(itemId<1 || itemId>items){
                throw new Exception("Item Id is out of range");
            } else if(rating<1 || rating >5){
                throw new Exception("Rating is out of range");
            }

            r[userId-1][itemId-1] = rating;

            int[] ratingEntry = new int[3];
            ratingEntry[0] = userId-1;
            ratingEntry[1] = itemId-1;
            ratingEntry[2] = rating;
            readRatings.add(ratingEntry);
            ratings = readRatings.size();

            line = b.readLine();
        }
    }

    public void initializePQ(){
        Random random = new Random();

        for(int k=0; k<K; k++){
            for(int i=0; i<users; i++){
                p[i][k] = random.nextDouble();
            }

            for(int j=0; j<items; j++){
                q[j][k] = random.nextDouble();
            }
        }
    }

    public double dotPQ(int i, int j){
        double result = 0;

        for(int k=0; k<K; k++){
            result += p[i][k] * q[j][k];
        }

        return result;
    }

    public double getNormalizer(int i, int j){
        double result = 0;

        for(int k=0; k<K; k++){
            result += Math.pow(p[i][k], 2) + Math.pow(q[j][k], 2);
        }

        return b/2 * result;
    }

    public void factorize(double[][] r, int steps, double errorThreshold){
        initializePQ();
        Double prevError = Double.POSITIVE_INFINITY;

        for(int s=0; s<steps; s++){
            double error = 0;

            for(int i=0; i<users; i++){
                for(int j=0; j<items; j++){
                    if(r[i][j] != 0) {
                        double dot = dotPQ(i, j);
                        double currentError = r[i][j] - dot;

                        for (int k = 0; k < K; k++) {
                            p[i][k] += a * (2 * currentError * q[j][k] - b * p[i][k]);
                            q[j][k] += a * (2 * currentError * p[i][k] - b * q[j][k]);
                        }
                    }
                }
            }

            for(int i=0; i<users; i++){
                for(int j=0; j<items; j++){
                    if(r[i][j] != 0){
                        double dot = dotPQ(i, j);
                        double norm = getNormalizer(i, j);
                        error += Math.pow(r[i][j] - dot, 2) + norm;
                    }
                }
            }

            double diff = error - prevError;

            if(diff >= -errorThreshold){
                break;
            }

            prevError = error;
        }
    }

    public void factorize(int steps, double errorThreshold){
        factorize(r, steps, errorThreshold);
    }

    public double[][] result(){
        double[][] res = new double[users][items];

        for(int i=0; i<users; i++){
            for(int j=0; j<items; j++){
                double dot = dotPQ(i,j);
                res[i][j] = dot > 5 ? 5 : dot;
            }
        }

        return res;
    }

    public void printResult(double[][] result){
        for(int i=0; i<users; i++){
            for(int j=0; j<items; j++){
                System.out.print(result[i][j] + " ");
            }
            System.out.println();
        }
    }

    public void printResultToFile(double[][] result) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter outFile = new PrintWriter("out.txt", "UTF-8");

        for(int i=0; i<users; i++){
            for(int j=0; j<items; j++){
                outFile.print(result[i][j] + " ");
            }

            outFile.println();
        }
    }

    public void printTestResultToFile(double[][] result, String testFilePath, String resultPath) throws IOException {
        PrintWriter resultFile = new PrintWriter(resultPath);
        BufferedReader b = new BufferedReader(new FileReader(testFilePath));

        String line = b.readLine();

        while(line != null){
            String[] parts = line.split(",");
            int userId = Integer.parseInt(parts[0]);
            int itemId = Integer.parseInt(parts[1]);

            resultFile.println(result[userId-1][itemId-1] > 5 ? 5 : result[userId-1][itemId-1]);

            line = b.readLine();
        }
    }

    public static void main(String args[]) throws Exception {
        Recommender recommender = new Recommender(Integer.parseInt(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
        //recommender.r = new double[][]{{5, 3, 0, 1}, {4, 0, 0, 1}, {1, 1, 0, 5}, {1, 0, 0, 4}, {0, 1, 5, 4}};
        recommender.readRatings(args[0]);
        //recommender.initializePQ();
        recommender.factorize(500, 0.001);
        //recommender.printResultToFile(recommender.result());
        recommender.printTestResultToFile(recommender.result(), args[1], "result.csv");
    }
}

package utils;

/**
 * Created by Neeraj on 3/29/2017.
 */
public class ProbabiltyEstimator {

    //P(triangle) = E(i=0 to d-1 ) P(i).N(i)
    //P(i) = (M/m)^(i+1) * (1- M/m)^(d-i-1)
    //N(i) = 1 - (1-(1/d))^(i+1)

    public static double getProbability(int M_int, int m_int, int d_int){


        double M = (double)M_int;
        double m = (double)m_int;
        double d = (double)d_int;

        double prob = 0;
        for(int i=0;i<d;i++){
            double p_i = Math.pow(M/m,i+1) * Math.pow( 1-(M/m), d-i-1);
            double n_i = 1 -  Math.pow(1-(1/d),i+1);
            prob += (p_i * n_i);
        }

        return prob;
    }

    /*****  M /(m*d) ******/
    public static double approx1(int M_int, int m_int, int d_int){

        double M = (double)M_int;
        double m = (double)m_int;
        double d = (double)d_int;
        return  M*d/(m);
    }

    /***** (1/ d*m^d) * Sum i=0 to d-1 [M^i(m-M)^(d-i-1)]    ******/
    public static double approx2(int M_int, int m_int, int d_int){
        double M = (double)M_int;
        double m = (double)m_int;
        double d = (double)d_int;

        double m_M = m-M;
        double productTerm = 1;
        for(int i=0;i<d;i++){
            productTerm+= Math.pow(M,i)* Math.pow((m_M),(d-i-1));
        }
        return (productTerm*M)/(d*Math.pow(m,d));
    }

    public static void main(String args[]){

        int parameters[][] = {
                                {50,3000,5},
                                {100,3000,5},//{ memory, noOfEdges, degree}
                                {100,3000,10},
                                {100,3000,50},
                                {300,3000,5},
                                {1000,3000,5},
                                {1000,3000,5},
                                {1000,3000,10},
                                {500,10000,5},
                                {500,10000,10},
                                {1000,10000,5},
                                {1000,10000,15}

        };
        System.out.format("\n%-10s%-10s%-10s%-30s%-30s%-30s%-30s%-30s",
                "M","m","d","probablity","approx1=(M/(md))", "Diff1 %age","approx2=(M/(md))", "Diff2 %age");

        for(int i=0;i<parameters.length;i++){

            double actualProb = getProbability(parameters[i][0],parameters[i][1],parameters[i][2]);
            double approx1Prob = approx1(parameters[i][0],parameters[i][1],parameters[i][2]);
            double approx2Prob = approx2(parameters[i][0],parameters[i][1],parameters[i][2]);
            double diff1 = 100*(actualProb - approx1Prob) / actualProb;
            double diff2 = 100*(actualProb - approx2Prob) / actualProb;

            System.out.format("\n%-10s%-10s%-10s%-30s%-30s%-30s%-30s%-30s",parameters[i][0],parameters[i][1], parameters[i][2],
                    actualProb ,

                    approx1Prob,
                    diff1,

                    approx2Prob,
                    diff2
                    );
        }
    }

}

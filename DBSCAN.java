/**
 * YangJie
 * 2018年12月14日上午10:39:06
 */
package yj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * <p>
 * test
 * </p>
 * <p>
 * Description:
 * </p>
 * 
 * @author YangJie
 * @data 2018年12月14日上午10:39:06
 * @version 1.0
 */

/*
 * think: use map save the inputValues can ues the key to index and can delete
 * the clusted Iteam
 */
public class DBSCAN {
    /** use to split the input to item what we need */
    private final static String ITEM_SPLIT = ",";

    /** atribute number of the iteam | 属性个数 */
    private final static int ATRIBUTE_NUMBER = 4;

    /**
     * the square of maximum distance of values to be considered as cluster | eps的平方
     */
    private double epsilon = 0.15;

    /** minimum number of members to consider cluster | minPts */
    private int minimumNumberOfClusterMembers = 8;

    /** distance metric applied for clustering **/
    // private DistanceMetric<V> metric = null;

    /** internal list of input values to be clustered */
    private ArrayList<Double[]> inputValues = null;

    /** index maintaining visited points */
    private ArrayList<Double[]> visitedPoints = new ArrayList<Double[]>();
    private ArrayList<Double[]> corePoints = new ArrayList<Double[]>();// 核心对象
    private int cid = 0;// 初始化聚类簇数
    private ArrayList<Double[]> visitedPointsOld = new ArrayList<Double[]>();// 记录当前每次簇形成时访问的样本集合
    private Queue<Double[]> Q = new LinkedList<Double[]>();// 队列Q

    public static void main(String[] args) {
        DBSCAN one = new DBSCAN();
        String fileAdd = "G:\\unvsty\\数据仓库与数据挖掘\\DBSCAN\\src\\yj\\iris.txt";
        ArrayList<ArrayList<Double[]>> result = null; // use to save the clustering comeout
        try {
            one.inputValues = one.readFile(fileAdd);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        result = one.performClustering();
        printCluster(result);
    }

    /**
     * From file read input to change it to we need iteam.
     * 
     * @param fileAdd
     * @return collection of all input iteam
     * @throws IOException
     */
    private ArrayList<Double[]> readFile(String fileAdd) throws IOException {

        ArrayList<Double[]> arrayList = new ArrayList<Double[]>();
        File file = new File(fileAdd);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 需要注意读入空行
                /* System.out.println(tempString); */
                String[] strings = tempString.split(ITEM_SPLIT);
                Double[] array = new Double[ATRIBUTE_NUMBER];
                for (int i = 0; i < ATRIBUTE_NUMBER; i++) {
                    array[i] = Double.valueOf(strings[i].toString());
                }
                arrayList.add(array);
            }
            reader.close();
            return arrayList;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
    }

    /**
     * Applies the clustering and returns a collection of clusters.
     * 
     * @return a list of lists of the respective cluster members.
     */
    public ArrayList<ArrayList<Double[]>> performClustering() {
        ArrayList<ArrayList<Double[]>> cluster = new ArrayList<ArrayList<Double[]>>();
        int bug = 0;

        findCorePoints();// 找出所有核心对象
        System.out.println("number of corepoints: "+corePoints.size());
        while (corePoints.size() != 0) {
            /*if(bug!=0) {System.out.println("cluster number: "+cluster.get(0).size());}*/
            visitedPointsOld = new ArrayList<Double[]>();
            initQueue();// 随机选取一个核心对象，初始化队列Q
            while (Q.size() != 0) {
                Double[] q = Q.poll();
                /*
                 * System.out.print("taken from header of Q: " + q[0] + " " + q[1] + " " + q[2]
                 * + " " + q[3]);
                 */
                if (findEpsPNumber(q) >= minimumNumberOfClusterMembers) {
                    ArrayList<Double[]> newQObj = new ArrayList<Double[]>();
                    newQObj = findEps(q);
                    for (int i = 0; i < newQObj.size(); i++) {
                        if (!(vpContains(newQObj.get(i)))) {
                            Q.offer(newQObj.get(i));
                            visitedPoints.add(newQObj.get(i));
                            visitedPointsOld.add(newQObj.get(i));
                        }
                    }
                }
            }
            /*System.out.println("number of corepoints: " + corePoints.size());*/
            cid++;
            /*System.out.println("--------------------visitedPointsOld---------------------");
            for (int i = 0; i < visitedPointsOld.size(); i++) {
                System.out.println(visitedPointsOld.get(i)[0] + " " + visitedPointsOld.get(i)[1] + " "
                        + visitedPointsOld.get(i)[2] + " " + visitedPointsOld.get(i)[3] + " ");
            }*/
            cluster.add(visitedPointsOld);
            for (Double[] o : visitedPointsOld) {
                if (cpContains(o)) {
                    Iterator<Double[]> iter = corePoints.iterator();
                    while (iter.hasNext()) {
                        Double[] next = iter.next();
                        if (next[0].equals(o[0]) && next[1].equals(o[1]) && next[2].equals(o[2])
                                && next[3].equals(o[3])) {
                            iter.remove();
                        }
                    }
                }
            }
            /*System.out.println("cluster number: "+cluster.get(0).size());*/
            bug++;
        }
        return cluster;
    }

    public void findCorePoints() {
        for (int i = 0; i < inputValues.size(); i++) {
            Double[] point1 = inputValues.get(i);
            int reachedPointsNumber = 0;
            for (int j = 0; j < inputValues.size(); j++) {
                Double[] point2 = inputValues.get(j);
                double distance = Math.pow((point1[0] - point2[0]), 2) + Math.pow((point1[1] - point2[1]), 2)
                        + Math.pow((point1[2] - point2[2]), 2) + Math.pow((point1[3] - point2[3]), 2);
                if (distance <= epsilon && distance > 0) {
                    reachedPointsNumber++;
                }
            }
            if (reachedPointsNumber >= minimumNumberOfClusterMembers) {
                corePoints.add(point1);
            }
        }
    }

    public void initQueue() {
        // 随机选取一个核心对象
        int n = corePoints.size();
        Q.clear();

        n = (int) (Math.random() * n);
        Double[] o = new Double[4];
        for (int i = 0; i < 4; i++) {
            o[i] = corePoints.get(n)[i];
        }
        Q.offer(o);
        visitedPoints.add(o);
        visitedPointsOld.add(o);
    }

    /* 找某个对象eps邻域内的所有对象 */
    public ArrayList<Double[]> findEps(Double[] q) {
        ArrayList<Double[]> epsPoints = new ArrayList<Double[]>();
        for (int i = 0; i < inputValues.size(); i++) {
            if (!(inputValues.get(i)[0].equals(q[0]) && inputValues.get(i)[1].equals(q[1])
                    && inputValues.get(i)[2].equals(q[2]) && inputValues.get(i)[3].equals(q[3]))) {
                double distance = Math.pow((q[0] - inputValues.get(i)[0]), 2)
                        + Math.pow((q[1] - inputValues.get(i)[1]), 2) + Math.pow((q[2] - inputValues.get(i)[2]), 2)
                        + Math.pow((q[3] - inputValues.get(i)[3]), 2);
                if (distance <= epsilon) {
                    epsPoints.add(inputValues.get(i));
                }
            }
        }
        return epsPoints;
    }

    public static void printCluster(ArrayList<ArrayList<Double[]>> cluster) {
        for (int i = 0; i < cluster.size(); i++) {
            System.out.println("---------------cluster" + (i + 1) + "----------------");
            System.out.println("number of cluster"+(i+1)+": "+cluster.get(i).size());
            for (Double[] o : cluster.get(i)) {
                System.out.print(o[0] + " " + o[1] + " " + o[2] + " " + o[3] + " ");
            }
            System.out.println();
        }
    }

    public Boolean cpContains(Double[] q) {
        for (int i = 0; i < corePoints.size(); i++) {
            if (q[0].equals(corePoints.get(i)[0]) && q[1].equals(corePoints.get(i)[1])
                    && q[2].equals(corePoints.get(i)[2]) && q[3].equals(corePoints.get(i)[3])) {
                return true;
            }
        }
        return false;
    }

    public Boolean vpContains(Double[] q) {
        for (int i = 0; i < visitedPoints.size(); i++) {
            if (q[0].equals(visitedPoints.get(i)[0]) && q[1].equals(visitedPoints.get(i)[1])
                    && q[2].equals(visitedPoints.get(i)[2]) && q[3].equals(visitedPoints.get(i)[3])) {
                return true;
            }
        }
        return false;
    }

    public int findEpsPNumber(Double[] q) {
        int number = 0;
        for (int i = 0; i < inputValues.size(); i++) {
            double distance = Math.pow((q[0] - inputValues.get(i)[0]), 2) + Math.pow((q[1] - inputValues.get(i)[1]), 2)
                    + Math.pow((q[2] - inputValues.get(i)[2]), 2) + Math.pow((q[3] - inputValues.get(i)[3]), 2);
            if (distance <= epsilon) {
                number++;
            }
        }
        return number;
    }
}

package com.wukong.mapreduce.friends;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * 原文：https://blog.csdn.net/H_Shun/article/details/78697837
 * https://blog.csdn.net/jin6872115/article/details/79586462
 * 上面的两篇文章不一样
 *
 * 为什么进行两部操作,因为这可能A的好友没有I  但是I的好友有A,这两个人没有互相关注.
 * @author fan
 */
public class SameFriendsMr2 {


    /**
     * 第一个Mapper
     * @author fan
     */
    private static class StepOneMapper extends Mapper<LongWritable, Text, Text, Text> {
        @Override
        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            // 传进来的 value: A:B,C,D,F,E,O
            // 将 value 转换成字符串,line 表示行数据，为"A:B,C,D,F,E,O"
            String line = value.toString();

            // 分割字符串,得到用户和好友们，用 userAndFriends表示，为{"A","B,C,D,F,E,O"}
            String[] userAndFriends = line.split(":");

            // userAndFriends 0位置为用户，user为"A"
            String user = userAndFriends[0];

            // userAndFriends 1位置为好友们，这里以","分割分割字符串，得到每一个好友
            // friend为{"B","C","D","F","E","O"}
            String[] friends = userAndFriends[1].split(",");

            // 循环遍历，以<B,A>,<C,A>,<D,A>......的形式传给reducer
            for (String friend : friends) {

                context.write(new Text(friend), new Text(user));

            }

        }

    }




    /**
     * 第二个Mapper
     * @author fan
     */
    private static  class StepOneReducer extends Reducer<Text, Text, Text, Text> {

        @Override
        public void reduce(Text friend, Iterable<Text> users, Context context)
                throws IOException, InterruptedException {

            // 传进来的数据 <好友，用户> <B,A>,<C,A>,<D,A>,<A,B>,<C,B>,<E,B>,<K,B>......
            // 新建 stringBuffer, 用于存放 拥有该好友的用户们
            StringBuffer stringBuffer = new StringBuffer();

            // 遍历所有的用户，并将用户放在stringBuffer中，以","分隔
            for (Text user : users) {
                stringBuffer.append(user).append(",");
            }

            //去掉最后一个逗号
            String vv = stringBuffer.substring(0, stringBuffer.length() - 1);
            // 以好友为key,用户们为value传给下一个mapper
            context.write(friend, new Text(vv));

        }

    }

    /**
     * 第一个Reducer
     * @author fan
     */
    private static  class StepTwoMapper extends Mapper<LongWritable, Text, Text, Text>{
        @Override
        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            // 传进来的数据      A    I,K,C,B,G,F,H,O,D,
            // 将传进来的数据转换成字符串并以"\t"分割，得到fiendsAndUsers
            String[] friendAndUsers = value.toString().split("\t");

            // fiendsAndUsers 0位置为好友  "A"
            String friend = friendAndUsers[0];

            // fiendsAndUsers 1位置为拥有上面好友用户们
            // 以 ","进行分割字符串，得到每一个用户,{"I","K",....}
            String[] users = friendAndUsers[1].split(",");

            // 将user进行排序，避免重复
            Arrays.sort(users);

            // 以用户-用户为key，好友们做value传给reducer
            for(int i=0; i<users.length-1; i++) {
                for(int j=i+1; j<users.length; j++) {
                    context.write(new Text(users[i]  + "-" + users[j]), new Text(friend));
                }
            }

        }

    }

    /**
     * 第一个Reducer
     * @author fan
     */

    private static  class StepTwoReducer extends Reducer<Text, Text, Text, Text>{
        @Override
        public void reduce(Text user_user, Iterable<Text> friends, Context context)
                throws IOException, InterruptedException {

            // 传进来的数据 <用户1-用户2，好友们>
            // 新建 stringBuffer, 用于用户的共同好友们
            StringBuffer stringBuffer = new StringBuffer();

            // 遍历所有的好友，并将这些好友放在stringBuffer中，以" "分隔
            for (Text friend : friends) {
                stringBuffer.append(friend).append(" ");
            }

            // 以好友为key,用户们为value传给下一个mapper
            context.write(user_user, new Text(stringBuffer.toString()));

        }

    }


    public static void main(String[] args) throws Exception {

        //提前删除输出目录
        File outPutDir = new File(args[1]);
        if (outPutDir.exists()) {
            FileUtils.deleteDirectory(outPutDir);
        }

        File outPutDir2 = new File(args[1]+"-2");
        if (outPutDir2.exists()) {
            FileUtils.deleteDirectory(outPutDir2);
        }


        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "求共同好友第一步");

        job.setJarByClass(SameFriendsMr2.class);
        job.setMapperClass(StepOneMapper.class);
        job.setReducerClass(StepOneReducer.class);


        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));


        //第二步开始

        Job job2 = Job.getInstance(conf,"求共同好友第二步");

        job2.setJarByClass(SameFriendsMr2.class);
        job2.setMapperClass(SameFriendsMr2.StepTwoMapper.class);
        job2.setReducerClass(SameFriendsMr2.StepTwoReducer.class);

        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);


        FileInputFormat.setInputPaths(job2, new Path(args[1]));
        FileOutputFormat.setOutputPath(job2, new Path(args[1]+"-2"));

        //联合执行
        ControlledJob aJob = new ControlledJob(job.getConfiguration());
        ControlledJob bJob = new ControlledJob(job2.getConfiguration());
        aJob.setJob(job);
        bJob.setJob(job2);
        //指定依赖关系
        bJob.addDependingJob(aJob);


        JobControl jc = new JobControl("jcF");
        jc.addJob(aJob);
        jc.addJob(bJob);

        Thread thread = new Thread(jc);
        thread.start();
        while(!jc.allFinished()){
            Thread.sleep(1000);
        }
        jc.stop();





    }





}

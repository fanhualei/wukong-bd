package com.wukong.mapreduce.friends;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.File;
import java.io.IOException;

/**
 * 求共同好友
 * 这种算法是错误的,应该进行两部扫做
 * @author fan
 */
public class SameFriendsMr {

    private static class MyMapper extends Mapper<Object, Text, Text, Text> {
        @Override
        public void map(Object inKey, Text inValue, Context context) throws IOException, InterruptedException {
            //A:B,C,D,F,E,O
            String files[] = inValue.toString().trim().split(":");
            String numbers[] = files[1].split(",");

            for (int i = 0; i < numbers.length; i++) {
                for (int j = i + 1; j < numbers.length; j++) {
                    String outKeyStr = numbers[i] + "-" + numbers[j];
                    int c = numbers[i].compareTo(numbers[j]);
                    if (c == 0) {
                        throw new InterruptedException("重复内容:" + inValue.toString());
                    }
                    if (c > 0) {
                        outKeyStr = numbers[j] + "-" + numbers[i];
                    }

                    context.write(new Text(outKeyStr), new Text(files[0]));
                }
            }
        }
    }


    private static class MyReduce extends Reducer<Text, Text, Text, Text> {
        @Override
        public void reduce(Text inKey, Iterable<Text> inValues, Context context) throws IOException, InterruptedException {
            StringBuffer sb=new StringBuffer();
            for(Text value:inValues){
                sb.append(value.toString()).append(",");
            }
            context.write(new Text(inKey.toString()+":"), new Text(sb.toString()));
        }
    }


    public static void main(String[] args) throws Exception {

        //提前删除输出目录
        File outPutDir = new File(args[1]);
        if (outPutDir.exists()) {
            FileUtils.deleteDirectory(outPutDir);
        }


        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "求共同好友");

        job.setJarByClass(SameFriendsMr.class);
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReduce.class);


        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);


    }


}

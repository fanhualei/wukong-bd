package com.wukong.mapreduce.pv;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.File;
import java.io.IOException;

/**
 * 指定时间与页面,统计访问情况
 * 这个例子就不写了
 */
public class PvWhere {
    private static class MyMapper extends Mapper<LongWritable, Text, Text, Text> {

        private String page,time;

        @Override
        public void setup(Context context){
            this.page=context.getConfiguration().getStrings("page", "page10")[0];
            this.time=context.getConfiguration().getStrings("time", "10:00")[0];
        }


        @Override
        protected void map(LongWritable key, Text value,Context context)
                throws IOException, InterruptedException {
            //page4	10:00	u2	0.4
            String [] reads = value.toString().trim().split("\t");
            //key=page+时间段

            if(reads[0].equals(page)&&reads[1].equals(time)){
                context.write(new Text(reads[0] + "\t" + reads[1]+ "\t" +reads[2])
                        , new Text(reads[3]));
            }
        }
    }


    private static class MyReducer extends Reducer<Text, Text, Text, Text> {
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            int accessNum = 0;
            String[] reads = key.toString().trim().split("\t");
            double sumtime = 0;
            //统计不同用户的访问次数以及访问时间
            for (Text vin : values) {
                accessNum++;
                sumtime += Double.parseDouble(vin.toString());
            }
            String kk = key.toString() + "\t" + accessNum;
            context.write(new Text(kk), new Text("" + sumtime));
        }
    }

    public static void main(String[] args) throws Exception {

        //提前删除输出目录
        File outPutDir = new File(args[1]);
        if (outPutDir.exists()) {
            FileUtils.deleteDirectory(outPutDir);
        }


        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "各个时间段,每个页面的访问次数");



        job.setJarByClass(PvWhere.class);
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);



        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);


        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);


    }







}

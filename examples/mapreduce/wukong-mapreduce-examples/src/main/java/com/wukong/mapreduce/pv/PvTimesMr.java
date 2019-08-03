package com.wukong.mapreduce.pv;

import com.wukong.mapreduce.version.VersionChangeMr;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

import java.io.File;
import java.io.IOException;

/**
 * 各个时间段,每个页面的访问次数
 *
 */
public class PvTimesMr {

    private static class MyMapper extends Mapper<LongWritable, Text, Text, NullWritable> {
        @Override
        protected void map(LongWritable key, Text value,Context context)
                throws IOException, InterruptedException {
            //page4	10:00	u2	0.4
            String [] reads = value.toString().trim().split("\t");
            //key=page+时间段
            context.write(new Text(reads[0] + "\t" + reads[1])
                    , NullWritable.get());
        }
    }




    private static  class  MyReducer extends Reducer<Text,NullWritable,Text,NullWritable>{
        @Override
        public void reduce(Text key, Iterable<NullWritable> inValues,Context context) throws IOException,InterruptedException{

            int accessNum = 0;
            //统计不同时间段不同表的访问次数
            for(NullWritable vin : inValues){
                accessNum ++;
            }
            context.write(new Text(key.toString() + "\t" + accessNum)
                    , NullWritable.get());

            multipleOutputs.write(new Text(key.toString() + "\t" + accessNum),NullWritable.get(),getTime(key));
        }

        private String getTime(Text key){
            String[] fir=key.toString().trim().split("\t");
            String[] times=fir[1].trim().split(":");
            return times[0];
        }

        /**
         * 文件输出
         */
        private MultipleOutputs<Text,NullWritable> multipleOutputs;
        @Override
        protected void setup(Context context){
            multipleOutputs = new MultipleOutputs<Text, NullWritable>(context);
        }
        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException{
            multipleOutputs.close();
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

        job.setJarByClass(VersionChangeMr.class);
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);



        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NullWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);


        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);


    }


}

package com.wukong.mapreduce.distinct;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.File;
import java.io.IOException;

public class DistinctMR {

    private static class MyMapper extends Mapper<Object,Text,Text,NullWritable>{
        @Override
        public void map(Object inKey,Text inValue,Context context)throws IOException,InterruptedException{
            context.write(inValue,NullWritable.get());
        }
    }

    private static class MyReduce extends Reducer<Text,NullWritable,Text,NullWritable>{
        @Override
        public void reduce(Text inkey,Iterable<NullWritable> inValues,Context context) throws IOException,InterruptedException{
            context.write(inkey,NullWritable.get());
        }
    }


    public static void main(String[] args) throws  Exception{

        File  outPutDir=new File(args[1]);
        if(outPutDir.exists()){
            FileUtils.deleteDirectory(outPutDir);
        }


        Configuration conf=new Configuration();
        Job job=Job.getInstance(conf,"简单去重");

        job.setJarByClass(DistinctMR.class);
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReduce.class);


        job.setMapOutputValueClass(NullWritable.class);
        job.setOutputValueClass(NullWritable.class);
        job.setOutputKeyClass(Text.class);

        FileInputFormat.addInputPath(job,new Path(args[0]));
        FileOutputFormat.setOutputPath(job,new Path(args[1]));

        System.exit(job.waitForCompletion(true)?0:1);

    }


}

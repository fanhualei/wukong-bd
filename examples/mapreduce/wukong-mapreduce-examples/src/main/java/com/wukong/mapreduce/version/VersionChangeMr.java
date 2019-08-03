package com.wukong.mapreduce.version;

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

public class VersionChangeMr {

    private static class MyMapper extends
            Mapper<Object,Text,VersionInfoWritable,NullWritable>{
        @Override
        protected void map(Object key, Text value,Context context)throws IOException, InterruptedException {
            String [] reads = value.toString().trim().split(",");
            //20170309,徐峥,光环斗地主,15,360手机助手,0.4版本,北京
            VersionInfoWritable version = new VersionInfoWritable(reads[0],reads[1],reads[2],Integer.parseInt(reads[3])
                    ,reads[4],reads[5],reads[6]);
            context.write(version, NullWritable.get());
        }
    }


    private static class MyReduce extends
            Reducer<VersionInfoWritable,NullWritable,Text,NullWritable>{
        @Override
        protected void reduce(VersionInfoWritable key, Iterable<NullWritable> values, Context context)
                throws IOException, InterruptedException {
            int count = 0;
            //记录初始版本号
            String OldVersion = "";

            for(NullWritable niv : values) {
                count++;
                if (count == 1) {
                    OldVersion=key.getVersion();
                    context.write(new Text(key.toString()),NullWritable.get());
                }else{
                    if(OldVersion.equalsIgnoreCase(key.getVersion())){
                        context.write(new Text(key.toString()),NullWritable.get());
                    }else{
                        context.write(new Text(key.toString()+","+OldVersion),NullWritable.get());
                        OldVersion=key.getVersion();
                    }
                }
            }
        }
    }


    public static void main(String[] args) throws Exception {

        //提前删除输出目录
        File outPutDir = new File(args[1]);
        if (outPutDir.exists()) {
            FileUtils.deleteDirectory(outPutDir);
        }


        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "求版本变化信息");

        job.setJarByClass(VersionChangeMr.class);
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReduce.class);



        job.setMapOutputKeyClass(VersionInfoWritable.class);
        job.setMapOutputValueClass(NullWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        job.setGroupingComparatorClass(VersionGroup.class);

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);


    }


}

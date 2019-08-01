package com.wukong.mapreduce;

import com.wukong.mapreduce.score.CourseScoreMaxMinAvg;
import org.apache.hadoop.util.ProgramDriver;

public class ExampleDriver {
    public static void main(String argv[]) {
        int exitCode = -1;
        ProgramDriver pgd = new ProgramDriver();
        try {
            pgd.addClass("wordcount", WordCount.class,
                    "查询文字个数");
            pgd.addClass("CourseScoreMaxMinAvg", CourseScoreMaxMinAvg.class,
                    "一个科目成绩的 最大值 最小值 平均值");
            exitCode = pgd.run(argv);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        System.exit(exitCode);
    }
}

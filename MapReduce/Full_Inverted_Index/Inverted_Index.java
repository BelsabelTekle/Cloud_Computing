import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


public class InvertedIndex extends Configured implements Tool{

	public static class InvertedIndexMapper extends 
				Mapper<LongWritable, Text, Text, IntWritable> {
		
		public static final String MalformedData = "MALFORMED";
		
		private Text outkey = new Text();
		private IntWritable outvalue = new IntWritable();

		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			FileSplit fileSplit = (FileSplit)context.getInputSplit();
			String filename = fileSplit.getPath().getName();
			//System.out.println("File name "+filename);
			//System.out.println("Directory and File name"+fileSplit.getPath().toString());
			
			String line = value.toString();
			StringTokenizer tokenizer = new StringTokenizer(line);
			while (tokenizer.hasMoreTokens()) {
				String word = tokenizer.nextToken().trim();
				if(word.equals("#")){
					context.getCounter(MalformedData, word).increment(1);
				}
				else{
					outkey.set(word);
					outvalue = new IntWritable(
							Integer.parseInt(filename.substring(4, filename.length()-4)));
					System.out.println(outkey+"  "+outvalue);
					context.write(outkey, outvalue);
				}			
			}
		}
	}
	
	public static class InvertedIndexReducer extends
	Reducer<Text, IntWritable, Text, Text> {
private Text outputkey = new Text();
private List<Integer> outputvalue = new ArrayList<Integer>();

public void reduce(Text key, Iterable<IntWritable> values,
		Context context) throws IOException, InterruptedException {
	outputkey = key;
	outputvalue = new ArrayList<Integer>();;
	for (IntWritable val : values) {
		if(!outputvalue.contains(val.get())){
			outputvalue.add(val.get());
		}
	}
	context.write(outputkey, new Text(outputvalue.toString()));
}
}
	
	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new Configuration(), new InvertedIndex(), args);
		System.exit(exitCode);
	}

	@Override
	public int run(String[] args) throws Exception {
		if (args.length != 2){
			System.out.printf("Usage: %s [generic options] <input> <output>\n", 
					getClass().getSimpleName());
			return -1;
		}
		Job job = new Job(getConf(), "InvertedIndex");
		job.setJarByClass(InvertedIndex.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setMapperClass(InvertedIndexMapper.class);
		job.setReducerClass(InvertedIndexReducer.class);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		boolean success = job.waitForCompletion(true);
		if(success){
			for(Counter counter: job.getCounters().getGroup(
					InvertedIndexMapper.MalformedData)){
						System.out.println(counter.getDisplayName()+"\t"+counter.getValue());
					}
		}
		return success ? 0 : 1;
	}
	
}

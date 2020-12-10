import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class InvertedIndex {

    public static class TokenizerMapper extends Mapper<Object, Text, Text, Text> {

        private Text word = new Text();
        private Text docID = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            StringTokenizer itr = new StringTokenizer(value.toString());
            docID.set(itr.nextToken().trim());

            while (itr.hasMoreTokens()) {
                String token = itr.nextToken();
                token = token.trim();
                token = token.toLowerCase();
//                token = token.replaceAll("[^0-9a-zA-z]", " ");
                char[] chars = token.toCharArray();
                for(int i = 0; i < token.length(); i++) {
                    char c = chars[i];
                    if(!((c <= 'z' && c >= 'a') || (c <= 'Z' && c >= 'A'))) {
                        chars[i] = ' ';
                    }
                }
                token = String.valueOf(chars);
                String[] ts = token.split(" ");
                for (String t : ts) {
                    t = t.trim();
                    if(t.equals("") || t.equals("\t")) continue;
                    word.set(t);
                    context.write(word, docID);
                }
            }
        }
    }


    public static class MyReducer extends Reducer<Text, Text, Text, Text> {

        private Text result = new Text();

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

            Map<String, Integer> map = new ConcurrentHashMap<>();

            for (Text docID : values) {
                // include some cases like docID = "500000000:1 5000000001:2"
                String docIDString = docID.toString();
                map.put(docIDString, map.getOrDefault(docIDString, 0) + 1);
            }
            for(String id : map.keySet()) {
                if(id.equals("") || id.equals("\t")) continue;
                if(id.contains(":")) {
                    // entry.format => docID:count
                    String[] entries = id.split("\t");
                    for (String entry : entries) {
                        String docID_cur = entry.split(":")[0];
                        Integer cnt = Integer.valueOf(entry.split(":")[1]);
                        if (map.containsKey(docID_cur)) {
                            map.put(docID_cur, cnt + map.get(docID_cur));
                        } else {
                            map.put(docID_cur, cnt);
                        }
                    }
                    map.remove(id);
                }
            }

            StringBuilder resString = new StringBuilder("");
            for(String docID : map.keySet()) {
                resString.append(docID + ":" + map.get(docID) + "\t");
            }

            result.set(resString.toString());
            context.write(key, result);
        }
    }


    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "inverted index");
        job.setJarByClass(InvertedIndex.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(MyReducer.class);
        job.setReducerClass(MyReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

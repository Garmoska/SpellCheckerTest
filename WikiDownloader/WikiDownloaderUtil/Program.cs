using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;

namespace WikiDownloaderUtil
{
    class Program
    {
        static void Main(string[] args)
        {
            var startupDir = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);
            var outputDir = Path.Combine(startupDir, "Output");
            var cacheDir = Path.Combine(outputDir, "Cache");
            var inputDir = Path.Combine(startupDir, "Input");
            var nlpSentModel = Path.Combine(Path.Combine(startupDir, "NLP"), "EnglishSD.nbin");

            var processFiles = Directory.GetFiles(inputDir, "*.txt");
            Console.WriteLine(string.Concat("Input directory: ", inputDir));
            Console.WriteLine(string.Empty);
            Console.WriteLine("Processing files:");
            for (var i = 0; i < processFiles.Length; i++)
            {
                Console.WriteLine(string.Format("   {0}) {1}", i + 1, Path.GetFileName(processFiles[i])));
            }
            Console.WriteLine(string.Empty);
            Console.WriteLine("Downloading and caching articles:");
            foreach (var file in processFiles)
            {
                var fileName = Path.GetFileName(file);
                Console.WriteLine(string.Empty);
                Console.WriteLine(string.Format("{0}...", fileName));
                var links = File.ReadAllLines(file);
                Console.WriteLine(string.Format("Found {0} links", links.Length));
                foreach (var link in links)
                {
                    var ind = link.LastIndexOf("/", StringComparison.InvariantCulture);
                    if (ind < 0) continue;
                    var fn = link.Substring(ind + 1);
                    var cachedFile = Path.Combine(cacheDir, Path.ChangeExtension(fn, "txt"));
                    if (File.Exists(cachedFile))
                    {
                        Console.WriteLine(string.Format(
                            "File exists {0}. If you want to refresh the cache file then just remove it and re-run this application.",
                            cachedFile));
                    }
                    else
                    {
                        Console.WriteLine(string.Format("Download {0} ...", link));
                        var pList = PageDownloader.GetParagraphsFromText(link);
                        File.WriteAllLines(cachedFile, pList);
                        Console.WriteLine(string.Format("The article has been cached: {0}", cachedFile));
                    }
                }
            }

            /*
            Console.WriteLine(string.Empty);
            Console.WriteLine("Collecting all sentences from all files to one file...");
            var sentencesOutputFile = Path.Combine(outputDir, "1_en.txt");
            var cachedFiles = Directory.GetFiles(cacheDir, "*.txt");
            var sentencesList = new List<string>();

            foreach (var cf in cachedFiles)
            {
                var lines = File.ReadAllLines(cf);
                foreach (var line in lines)
                {
                    var sentenceDetector = new SentenceDetectorTool();
                    sentenceDetector.
                    var sentences = sentenceDetector.SentenceDetect(paragraph);
                    var snts = line.Split(divider, StringSplitOptions.RemoveEmptyEntries);
                    sentencesList.AddRange(snts);
                }
            }
            File.WriteAllLines(sentencesOutputFile, sentencesList);
            Console.WriteLine(string.Format("The file {0} with all sentences (count: {1}) has been created.", sentencesOutputFile, sentencesList.Count));
            */

            Console.WriteLine(string.Empty);
            Console.Write("Press any button...");
            Console.ReadLine();
        }
    }
}

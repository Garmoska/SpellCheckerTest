using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Text;
using System.Xml;

namespace WikiDownloaderUtil
{
    public static class PageDownloader
    {
        public static string GetHtml(string url, out string errorString)
        {
            errorString = string.Empty;
            var result = string.Empty;
            try
            {
                var request = (HttpWebRequest)WebRequest.Create(url);
                var response = (HttpWebResponse)request.GetResponse();
                if (response.StatusCode == HttpStatusCode.OK)
                {
                    using (var receiveStream = response.GetResponseStream())
                    {
                        if (receiveStream != null)
                        {
                            var readStream = response.CharacterSet == null ? new StreamReader(receiveStream) : new StreamReader(receiveStream, Encoding.GetEncoding(response.CharacterSet));
                            result = readStream.ReadToEnd();
                            response.Close();
                        }
                    }
                }
            }
            catch (WebException exc)
            {
                errorString = string.Format("Can't receive data via URL = '{0}'. Error = '{1}'", url, exc.Message);
            }

            return result;
        }

        public static List<string> GetParagraphsFromText(string url)
        {
            string errStr;
            var html = GetHtml(url, out errStr);
            XmlDocument xml;
            try
            {
                xml = new XmlDocument();
                xml.LoadXml(html);
            }
            catch (Exception exc)
            {
                throw;
            }

            var pList = new List<string>();
            var xnList = xml.SelectNodes("/html/body/div[@id='content']/div[@id='bodyContent']/div[@id='mw-content-text']/div[@class='mw-parser-output']/p");
            if (xnList != null && xnList.Count > 0)
            {
                foreach (XmlNode node in xnList)
                {
                    var t = node.InnerText;//node.InnerXml;// 
                    if (string.IsNullOrEmpty(t)) continue;
                    pList.Add(t);
                }

            }
            return pList;
        }
    }
}

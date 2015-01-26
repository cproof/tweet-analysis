# tweet-analysis

This is a Sentiment-Analysis Tool for Twitter


## How to start the Program (Instructions for Ubuntu 14.04)
---

- Make sure you have all required packages installed or install them via apt-get
  - git
  - maven2
  - openjdk-7-jdk
- Download the sources from github.com/cproof/tweet-analysis
  - git clone https://github.com/cproof/tweet-analysis.git
- Add your Twitter credentials in at.tuwien.aic.tweetanalysis.provider.TwitterCredentials.java
- Go into the subfolder *server*
- Run **mvn clean install**
- Execute **mvn exec:java**
- Launch the *index.html* file from the *client* directory in your browser

---

## The MIT License

Copyright (c) 2015 Patrick Löwenstein, Thomas Schreiber, Alexander Suchan, Stefan Victora, Andreas Waltenberger https://github.com/cproof/tweet-analysis

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.


const express = require('express');
const router = express.Router();
const axios = require('axios');
const SolrAPI = 'http://localhost:8983/solr/cs572/select?q=';
const SolrSuggestAPI = 'http://localhost:8983/solr/cs572/suggest?q=';
const sortWithRank = '&sort=pageRankFile%20desc'
const fs = require('fs');
const path = require('path');
const rfs = require('fs').readFileSync

// refer to https://github.com/dscape/spell
var spell = require('spell');

// load the corpus for spell correction
var dict = spell();
let p = path.join(__dirname, "../resources/big.txt");
dict.load(rfs(p, { encoding: 'utf-8'} ).toString());

router.get('/', (req, res) => {
  res.send("it works");
})

// Get lucene result
router.get('/search/:id', (req, res) => {
  const params = req.params;
  let keywords = params.id;

  axios.get(`${SolrAPI}${keywords}`).then(posts => {
    // console.log(posts.data.response.docs);
    res.json(posts.data.response.docs);
  }).catch(error => {
    res.send(error);
  })
})

// Get page rank result
router.get('/rank/:id', (req, res) => {
  const params = req.params;
  let keywords = params.id;

  axios.get(`${SolrAPI}${keywords}${sortWithRank}`).then(posts => {
    addUrlToEach(posts.data.response.docs).then(docList => {
      res.json(docList);
    }).catch(error => {
      console.log(error);
    })
  }).catch(error => {
    res.send(error);
  })
})

// Get auto suggest from Solr
// refer to https://github.com/dscape/spell
router.get('/suggest/:q', (req, res) => {
  const params = req.params;
  let q = params.q;
  let prefix = '';
  if(q.includes(" ")) {
    prefix = q.split(' ')[0] + ' ';
    q = q.split(' ')[1];
  }
  let spellCheckRes = dict.lucky(q);
  if(spellCheckRes === undefined) {
    spellCheckRes = q;
  }

  // console.log(`${SolrSuggestAPI}${spellCheckRes}`);
  console.log(dict.lucky(q));

  axios.get(`${SolrSuggestAPI}${spellCheckRes}`).then(response => {
    let queryRes = response.data.suggest.suggest;
    // console.log('queryrES', queryRes);
    // console.log(queryRes.suggestions === undefined);
    for(let i = 0; i < queryRes[spellCheckRes].suggestions.length; i++) {
      let temp = queryRes[spellCheckRes].suggestions[i].term;
      queryRes[spellCheckRes].suggestions[i].term = prefix + temp;
    }
    res.json(queryRes[spellCheckRes].suggestions);
  })

  // let ans = dict.suggest('socer');
  // res.json(ans);

})

// Get recommend word for search
router.get('/recommend/:q', (req, res) => {
  const params = req.params;
  let q = params.q;
  let prefix = '';
  if(q.includes(" ")) {
    prefix = q.split(' ')[0] + ' ';
    q = q.split(' ')[1];
  }
  let spellCheckRes = dict.lucky(q);
  if(spellCheckRes === undefined) {
    spellCheckRes = q;
  }

  res.json(prefix + spellCheckRes);
})


async function addUrlToEach(docList) {

  let p = path.join(__dirname, "../resources/URLtoHTML_latimes_news.csv");
  let fileToUrlMap = await pReadFile(p);
  for(let i = 0; i < docList.length; i++) {
    let doc = docList[i];
    if(doc.description === undefined) {
      doc.description = 'N/A';
    }
    if(doc.og_url === undefined) {
      let startIdx = doc.id.lastIndexOf("/");
      console.log(doc.id.substring(startIdx + 1));
      let url = fileToUrlMap[doc.id.substring(startIdx + 1)];
      console.log('url', url);
      doc.og_url = url;
    }
  }
  return docList;
}

let pReadFile = function (filePath) {
  var fileToUrlMap = {};
  return new Promise(function (resolve, reject) {
    fs.readFile(filePath, 'utf-8', function (err, data) {
      if (err) {
        reject(err);
      }
      var dataArray = data.split(/\r?\n/);
      for(let i = 1; i < dataArray.length; i++) {
        let line = dataArray[i];
        let items = line.split(',');
        fileToUrlMap[items[0]] = items[1];
      }
      resolve(fileToUrlMap);
    });
  })
}

module.exports = router;

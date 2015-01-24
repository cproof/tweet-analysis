var baseUrl = "http://localhost:8080";

angular.module('sentiment', ['nvd3'])
        .controller('sentimentSearchController', function ($scope, $http) {
            $scope.query = "";
            $scope.count = 2;
            $scope.classifier = "smo";
            $scope.language = null;
            $scope.beginDate = null;
            $scope.endDate = null;
            $scope.location = null;
            $scope.radius = null;
            $scope.verbose = true;
            
            $scope.countOptions = [2,10,20,50,100,200,500];
            $scope.classifierOptions = {smo:"SMO",smoSmileys:"SMO using Smileys",bayes:"Naive Bayes",bayesSmileys:"Naive Bayes using Smileys"};
            $scope.languageOptions = {ab:"Abkhaz",aa:"Afar",af:"Afrikaans",ak:"Akan",sq:"Albanian",am:"Amharic",ar:"Arabic",an:"Aragonese",hy:"Armenian",as:"Assamese",av:"Avaric",ae:"Avestan",ay:"Aymara",az:"Azerbaijani",bm:"Bambara",ba:"Bashkir",eu:"Basque",be:"Belarusian",bn:"Bengali",bh:"Bihari",bi:"Bislama",bs:"Bosnian",br:"Breton",bg:"Bulgarian",my:"Burmese",ca:"Catalan; Valencian",ch:"Chamorro",ce:"Chechen",ny:"Chichewa; Chewa; Nyanja",zh:"Chinese",cv:"Chuvash",kw:"Cornish",co:"Corsican",cr:"Cree",hr:"Croatian",cs:"Czech",da:"Danish",dv:"Divehi; Dhivehi; Maldivian;",nl:"Dutch",en:"English",eo:"Esperanto",et:"Estonian",ee:"Ewe",fo:"Faroese",fj:"Fijian",fi:"Finnish",fr:"French",ff:"Fula; Fulah; Pulaar; Pular",gl:"Galician",ka:"Georgian",de:"German",el:"Greek, Modern",gn:"Guaraní",gu:"Gujarati",ht:"Haitian; Haitian Creole",ha:"Hausa",he:"Hebrew (modern)",hz:"Herero",hi:"Hindi",ho:"Hiri Motu",hu:"Hungarian",ia:"Interlingua",id:"Indonesian",ie:"Interlingue",ga:"Irish",ig:"Igbo",ik:"Inupiaq",io:"Ido",is:"Icelandic",it:"Italian",iu:"Inuktitut",ja:"Japanese",jv:"Javanese",kl:"Kalaallisut, Greenlandic",kn:"Kannada",kr:"Kanuri",ks:"Kashmiri",kk:"Kazakh",km:"Khmer",ki:"Kikuyu, Gikuyu",rw:"Kinyarwanda",ky:"Kirghiz, Kyrgyz",kv:"Komi",kg:"Kongo",ko:"Korean",ku:"Kurdish",kj:"Kwanyama, Kuanyama",la:"Latin",lb:"Luxembourgish, Letzeburgesch",lg:"Luganda",li:"Limburgish, Limburgan, Limburger",ln:"Lingala",lo:"Lao",lt:"Lithuanian",lu:"Luba-Katanga",lv:"Latvian",gv:"Manx",mk:"Macedonian",mg:"Malagasy",ms:"Malay",ml:"Malayalam",mt:"Maltese",mi:"Māori",mr:"Marathi (Marāṭhī)",mh:"Marshallese",mn:"Mongolian",na:"Nauru",nv:"Navajo, Navaho",nb:"Norwegian Bokmål",nd:"North Ndebele",ne:"Nepali",ng:"Ndonga",nn:"Norwegian Nynorsk",no:"Norwegian",ii:"Nuosu",nr:"South Ndebele",oc:"Occitan",oj:"Ojibwe, Ojibwa",cu:"Old Church Slavonic, Church Slavic, Church Slavonic, Old Bulgarian, Old Slavonic",om:"Oromo",or:"Oriya",os:"Ossetian, Ossetic",pa:"Panjabi, Punjabi",pi:"Pāli",fa:"Persian",pl:"Polish",ps:"Pashto, Pushto",pt:"Portuguese",qu:"Quechua",rm:"Romansh",rn:"Kirundi",ro:"Romanian, Moldavian, Moldovan",ru:"Russian",sa:"Sanskrit (Saṁskṛta)",sc:"Sardinian",sd:"Sindhi",se:"Northern Sami",sm:"Samoan",sg:"Sango",sr:"Serbian",gd:"Scottish Gaelic; Gaelic",sn:"Shona",si:"Sinhala, Sinhalese",sk:"Slovak",sl:"Slovene",so:"Somali",st:"Southern Sotho",es:"Spanish; Castilian",su:"Sundanese",sw:"Swahili",ss:"Swati",sv:"Swedish",ta:"Tamil",te:"Telugu",tg:"Tajik",th:"Thai",ti:"Tigrinya",bo:"Tibetan Standard, Tibetan, Central",tk:"Turkmen",tl:"Tagalog",tn:"Tswana",to:"Tonga (Tonga Islands)",tr:"Turkish",ts:"Tsonga",tt:"Tatar",tw:"Twi",ty:"Tahitian",ug:"Uighur, Uyghur",uk:"Ukrainian",ur:"Urdu",uz:"Uzbek",ve:"Venda",vi:"Vietnamese",vo:"Volapük",wa:"Walloon",cy:"Welsh",wo:"Wolof",fy:"Western Frisian",xh:"Xhosa",yi:"Yiddish",yo:"Yoruba",za:"Zhuang, Chuang"};
            
            $scope.currentlySearching = false; //disable form when performing a query
            $scope.languageFilterActive = false; 
            $scope.beginDateFilterActive = false; 
            $scope.endDateFilterActive = false; 
            $scope.locationFilterActive = false; 
            
            $scope.fetch = function () {
                $scope.sentQuery = $scope.query;
                $scope.currentlySearching = true;
                $scope.verboseTweets = null;
                
                $http.get(baseUrl + "/search?q=" + $scope.query + "&verbose=" + $scope.verbose + "&c=" + $scope.count + "&cl=" + $scope.classifier + ($scope.languageFilterActive && $scope.language != null ? "&l="+$scope.language : "") + ($scope.beginDateFilterActive && $scope.beginDate != null ? "&bd="+$scope.beginDate.toISOString().substring(0, 10) : "") + ($scope.endDateFilterActive && $scope.endDate != null ? "&ed="+$scope.endDate.toISOString().substring(0, 10) : "") + ($scope.locationFilterActive && $scope.location != null && $scope.radius != null ? "&gl="+$scope.location+"&r="+$scope.radius : ""))
                        .success(function (response) {
                            $scope.currentlySearching = false;
                            $scope.sentiment = response.sentiment;
                            $scope.verboseTweets = response.tweets;
                            $scope.diagrams.refreshAll();
                            //$scope.api.refresh();
                            $scope.api.update();
                            
                        });
            };
            
            $scope.impression = function() {
                if ($scope.sentiment > 0.8) {
                    return 'strongly positive';
                }
                else if ($scope.sentiment > 0.6) {
                    return 'positive';
                }
                else if ($scope.sentiment > 0.4) {
                    return 'neutral';
                }
                else if ($scope.sentiment > 0.2) {
                    return 'negative';
                }
                else {
                    return 'strongly negative';
                }
            };
            
            
            $scope.diagrams = {
                general: {
                    options: {
                        chart: {
                            type: 'bulletChart',
                            transitionDuration: 500,
                            height: 50,
                            tooltips: false 
                        }
                    },
                    
                    data : null,
                    
                    refresh : function() {
                        this.data = {
                            title: "Sentiment",
                            ranges: [0, 1],
                            measures: [$scope.sentiment],
                            markers: [$scope.sentiment]
                        }
                    }
                },
                
                refreshAll : function() {
                    this.general.refresh();
                }
            };
            
            $scope.diagrams.refreshAll();
            
        });
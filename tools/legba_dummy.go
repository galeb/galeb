package main

import (
	"bytes"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"flag"
	"fmt"
	"io"
	"io/ioutil"
	"math/rand"
	"os"
	"time"
	"unsafe"

	"github.com/gin-gonic/gin"
)

var (
	filename        = flag.String("filename", "", "use virtualhosts config")
	nVirtualHosts   = flag.Int("n", 1, "create N virtual hosts")
	fixedSize       = flag.Int("fixedSize", 1024, "bytes to reply")
	randomDelay     = flag.Int("randDelay", 0, "random delay in ms when replying")
	replyPayloadMin = flag.Int("minReply", 1, "min bytes to reply")
	replyPayloadMax = flag.Int("maxReply", 1, "max bytes to reply")
)

func init() {
	rand.Seed(time.Now().UnixNano())
}

// From:
// https://stackoverflow.com/questions/22892120/how-to-generate-a-random-string-of-a-fixed-length-in-go
//
const letterBytes = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
const (
	letterIdxBits = 6                    // 6 bits to represent a letter index
	letterIdxMask = 1<<letterIdxBits - 1 // All 1-bits, as many as letterIdxBits
	letterIdxMax  = 63 / letterIdxBits   // # of letter indices fitting in 63 bits
)

var src = rand.NewSource(time.Now().UnixNano())

func RandPayload(n int) string {
	b := make([]byte, n)
	// A src.Int63() generates 63 random bits, enough for letterIdxMax characters!
	for i, cache, remain := n-1, src.Int63(), letterIdxMax; i >= 0; {
		if remain == 0 {
			cache, remain = src.Int63(), letterIdxMax
		}
		if idx := int(cache & letterIdxMask); idx < len(letterBytes) {
			b[i] = letterBytes[idx]
			i--
		}
		cache >>= letterIdxBits
		remain--
	}

	return *(*string)(unsafe.Pointer(&b))
}

type VirtualHost struct {
	Id          int                    `json:"id"`
	Name        string                 `json:"name"`
	Properties  map[string]interface{} `json:"properties"`
	Hash        int                    `json:"hash"`
	Rules       []Rule                 `json:"rules"`
	Environment Environment            `json:"environment"`

	Version        *string `json:"_version"`
	CreatedBy      *string `json:"_created_by"`
	CreatedAt      *string `json:"_created_at"`
	LastModifiedBy *string `json:"_lastmodified_by"`
	LastModifiedAt *string `json:"_lastmodified_at"`
	Status         *string `json:"_status"`
}

type Environment struct {
	Id         int                    `json:"id"`
	Name       string                 `json:"name"`
	Properties map[string]interface{} `json:"properties"`
	Hash       int                    `json:"hash"`

	Version        *string `json:"_version"`
	CreatedBy      *string `json:"_created_by"`
	CreatedAt      *string `json:"_created_at"`
	LastModifiedBy *string `json:"_lastmodified_by"`
	LastModifiedAt *string `json:"_lastmodified_at"`
	Status         *string `json:"_status"`
}

type Rule struct {
	Id         int                    `json:"id"`
	Name       string                 `json:"name"`
	Properties map[string]interface{} `json:"properties"`
	Hash       int                    `json:"hash"`
	Pool       Pool                   `json:"pool"`
	Global     bool                   `json:"global"`

	RuleType RuleType `json:"ruleType"`

	Version        *string `json:"_version"`
	CreatedBy      *string `json:"_created_by"`
	CreatedAt      *string `json:"_created_at"`
	LastModifiedBy *string `json:"_lastmodified_by"`
	LastModifiedAt *string `json:"_lastmodified_at"`
	Status         *string `json:"_status"`
}

type RuleType struct {
	Id         int                    `json:"id"`
	Name       string                 `json:"name"`
	Properties map[string]interface{} `json:"properties"`
	Hash       int                    `json:"hash"`

	Version        *string `json:"_version"`
	CreatedBy      *string `json:"_created_by"`
	CreatedAt      *string `json:"_created_at"`
	LastModifiedBy *string `json:"_lastmodified_by"`
	LastModifiedAt *string `json:"_lastmodified_at"`
	Status         *string `json:"_status"`
}

type Pool struct {
	Id         int                    `json:"id"`
	Name       string                 `json:"name"`
	Hash       int                    `json:"hash"`
	Properties map[string]interface{} `json:"properties"`
	Targets    []Target               `json:"targets"`

	Version        *string `json:"_version"`
	CreatedBy      *string `json:"_created_by"`
	CreatedAt      *string `json:"_created_at"`
	LastModifiedBy *string `json:"_lastmodified_by"`
	LastModifiedAt *string `json:"_lastmodified_at"`
	Status         *string `json:"_status"`
}

type Target struct {
	Id         int                    `json:"id"`
	Name       string                 `json:"name"`
	Properties map[string]interface{} `json:"properties"`
	Hash       int                    `json:"hash"`

	Version        *string `json:"_version"`
	CreatedBy      *string `json:"_created_by"`
	CreatedAt      *string `json:"_created_at"`
	LastModifiedBy *string `json:"_lastmodified_by"`
	LastModifiedAt *string `json:"_lastmodified_at"`
	Status         *string `json:"_status"`
}

func (v *VirtualHost) GenerateHash() (string, error) {
	result := new(bytes.Buffer)
	enc := json.NewEncoder(result)
	enc.SetEscapeHTML(false)

	err := enc.Encode(v)
	if err != nil {
		return "", err
	}

	// JSON encoder adds a trailing `\n`
	b := result.Bytes()
	b = bytes.TrimSuffix(b, []byte("\n"))

	hash := sha256.New()
	if _, err := io.Copy(hash, bytes.NewReader(b)); err != nil {
		return "", err
	}
	return hex.EncodeToString(hash.Sum(nil)), nil
}

func CreateVHost(hostname string) *VirtualHost {
	r := rand.Intn(2000)
	vhost := &VirtualHost{
		Id:         rand.Intn(10000) + 1,
		Name:       hostname,
		Properties: map[string]interface{}{},
		Hash:       r + 1,
		Rules: []Rule{
			{
				Id:   r + 3,
				Name: fmt.Sprintf("rule-%s-%d", hostname, r+3),
				Properties: map[string]interface{}{
					"match": "/",
					"order": "1",
				},
				Hash:   r + 4,
				Global: false,
				RuleType: RuleType{
					Id:         0,
					Name:       "UrlPath",
					Properties: map[string]interface{}{},
					Hash:       1,
				},
				Pool: Pool{
					Id:         r + 4,
					Name:       fmt.Sprintf("pool-%s-%d", hostname, r+4),
					Properties: map[string]interface{}{},
					Hash:       r + 4,
					Targets: []Target{
						{
							Id:         r + 5,
							Name:       "http://localhost:8090",
							Properties: map[string]interface{}{},
							Hash:       r + 5,
						},
					},
				},
			},
		},
		Environment: Environment{
			Id:         r + 6,
			Properties: map[string]interface{}{},
			Hash:       r + 6,
		},
	}

	hash, _ := vhost.GenerateHash()
	vhost.Properties["fullhash"] = hash
	vhost.Environment.Properties["fullhash"] = hash

	return vhost
}

func GenDefaultConfig() []*VirtualHost {
	return []*VirtualHost{
		{
			Id:         3497,
			Name:       "vh.test.com",
			Properties: map[string]interface{}{"fullhash": "1234"},
			Hash:       3,
			Rules: []Rule{
				{
					Id:   32134,
					Name: "rule2-test-http",
					Properties: map[string]interface{}{
						"match": "/*.ghtml",
						"order": "1",
					},
					Hash:   5,
					Global: false,
					RuleType: RuleType{
						Id:         0,
						Name:       "UrlPath",
						Properties: map[string]interface{}{},
						Hash:       1,
					},
					Pool: Pool{
						Id:         1,
						Name:       "pool2-test-http",
						Properties: map[string]interface{}{},
						Hash:       3,
						Targets: []Target{
							{
								Id:         103748,
								Name:       "http://localhost:8090",
								Properties: map[string]interface{}{},
								Hash:       2,
							},
						},
					},
				},
				{
					Id:   63824,
					Name: "rule1-test-http",
					Properties: map[string]interface{}{
						"match": "/",
						"order": "2",
					},
					Hash:   5,
					Global: false,
					RuleType: RuleType{
						Id:         0,
						Name:       "UrlPath",
						Properties: map[string]interface{}{},
						Hash:       1,
					},
					Pool: Pool{
						Id:         0,
						Name:       "pool-test-http",
						Properties: map[string]interface{}{},
						Hash:       2,
						Targets: []Target{
							{
								Id:         103748,
								Name:       "http://localhost:8080",
								Properties: map[string]interface{}{},
								Hash:       2,
							},
						},
					},
				},
			},
			Environment: Environment{
				Id: 1,
				Properties: map[string]interface{}{
					"fullhash": "43212",
				},
				Hash: 2,
			},
		},
	}
}

func LoadFromFile(f string) []*VirtualHost {
	jsonFile, err := os.Open(f)
	if err != nil {
		fmt.Println(err)
	}
	defer jsonFile.Close()

	byteValue, _ := ioutil.ReadAll(jsonFile)

	var result struct {
		VirtualHosts []*VirtualHost `json:"virtualhosts"`
	}
	json.Unmarshal([]byte(byteValue), &result)

	return result.VirtualHosts
}

func main() {
	flag.Parse()

	var config []*VirtualHost
	if *filename != "" {
		config = LoadFromFile(*filename)
	} else {
		if *nVirtualHosts > 1 {
			for i := 0; i < *nVirtualHosts; i++ {
				config = append(config, CreateVHost(fmt.Sprintf("galeb-test-%d", i)))
			}
		} else {
			config = GenDefaultConfig()
		}
	}

	r := gin.Default()
	r.GET("/", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"message": "working",
		})
	})
	r.GET("/virtualhostscached/BE-LAB", func(c *gin.Context) {
		c.JSON(200, map[string]interface{}{
			"virtualhosts": config,
		})
	})
	r.POST("/routers", func(c *gin.Context) {
		c.JSON(200, map[string]interface{}{
			"virtualhosts": config,
		})
	})

	go func() {
		g := gin.Default()

		payload := RandPayload(*fixedSize)
		rand.Seed(time.Now().UnixNano())
		g.GET("/*giniscrazy", func(c *gin.Context) {
			if *randomDelay != 0 {
				time.Sleep(time.Duration(rand.Intn(*randomDelay)) * time.Millisecond)
			}

			if *replyPayloadMin != 1 && *replyPayloadMax != 1 {
				size := rand.Intn(*replyPayloadMax-*replyPayloadMin+1) + *replyPayloadMin
				payload = RandPayload(size)
			}

			c.String(200, payload)
		})
		err := g.Run("localhost:8090")
		if err != nil {
			panic(err)
		}
	}()

	err := r.Run("localhost:8080")
	if err != nil {
		panic(err)
	}
}

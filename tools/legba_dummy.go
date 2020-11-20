package main

import (
	"math/rand"
	"time"

	"github.com/gin-gonic/gin"
)

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

func GenConfig() []*VirtualHost {
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

func main() {
	config := GenConfig()

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

		rand.Seed(time.Now().UnixNano())
		g.GET("/*.ghtml", func(c *gin.Context) {
			time.Sleep(time.Duration(rand.Intn(10000)) * time.Millisecond)
			c.JSON(200, gin.H{
				"message": "working",
			})
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

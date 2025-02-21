package service

import (
	"github.com/crawlab-team/crawlab-core/interfaces"
	"github.com/crawlab-team/crawlab-core/models/models"
	"github.com/crawlab-team/crawlab-db/mongo"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type ModelService interface {
	interfaces.ModelService
	DropAll() (err error)
	GetNodeById(id primitive.ObjectID) (res *models.Node, err error)
	GetNode(query bson.M, opts *mongo.FindOptions) (res *models.Node, err error)
	GetNodeList(query bson.M, opts *mongo.FindOptions) (res []models.Node, err error)
	GetNodeByKey(key string, opts *mongo.FindOptions) (res *models.Node, err error)
	GetProjectById(id primitive.ObjectID) (res *models.Project, err error)
	GetProject(query bson.M, opts *mongo.FindOptions) (res *models.Project, err error)
	GetProjectList(query bson.M, opts *mongo.FindOptions) (res []models.Project, err error)
	GetArtifactById(id primitive.ObjectID) (res *models.Artifact, err error)
	GetArtifact(query bson.M, opts *mongo.FindOptions) (res *models.Artifact, err error)
	GetArtifactList(query bson.M, opts *mongo.FindOptions) (res []models.Artifact, err error)
	GetTagById(id primitive.ObjectID) (res *models.Tag, err error)
	GetTag(query bson.M, opts *mongo.FindOptions) (res *models.Tag, err error)
	GetTagList(query bson.M, opts *mongo.FindOptions) (res []models.Tag, err error)
	GetTagIds(colName string, tags []interfaces.Tag) (tagIds []primitive.ObjectID, err error)
	UpdateTagsById(colName string, id primitive.ObjectID, tags []interfaces.Tag) (tagIds []primitive.ObjectID, err error)
	UpdateTags(colName string, query bson.M, tags []interfaces.Tag) (tagIds []primitive.ObjectID, err error)
	GetJobById(id primitive.ObjectID) (res *models.Job, err error)
	GetJob(query bson.M, opts *mongo.FindOptions) (res *models.Job, err error)
	GetJobList(query bson.M, opts *mongo.FindOptions) (res []models.Job, err error)
	GetScheduleById(id primitive.ObjectID) (res *models.Schedule, err error)
	GetSchedule(query bson.M, opts *mongo.FindOptions) (res *models.Schedule, err error)
	GetScheduleList(query bson.M, opts *mongo.FindOptions) (res []models.Schedule, err error)
	GetUserById(id primitive.ObjectID) (res *models.User, err error)
	GetUser(query bson.M, opts *mongo.FindOptions) (res *models.User, err error)
	GetUserList(query bson.M, opts *mongo.FindOptions) (res []models.User, err error)
	GetUserByUsername(username string, opts *mongo.FindOptions) (res *models.User, err error)
	GetUserByUsernameWithPassword(username string, opts *mongo.FindOptions) (res *models.User, err error)
	GetSettingById(id primitive.ObjectID) (res *models.Setting, err error)
	GetSetting(query bson.M, opts *mongo.FindOptions) (res *models.Setting, err error)
	GetSettingList(query bson.M, opts *mongo.FindOptions) (res []models.Setting, err error)
	GetSettingByKey(key string, opts *mongo.FindOptions) (res *models.Setting, err error)
	GetSpiderById(id primitive.ObjectID) (res *models.Spider, err error)
	GetSpider(query bson.M, opts *mongo.FindOptions) (res *models.Spider, err error)
	GetSpiderList(query bson.M, opts *mongo.FindOptions) (res []models.Spider, err error)
	GetTaskById(id primitive.ObjectID) (res *models.Task, err error)
	GetTask(query bson.M, opts *mongo.FindOptions) (res *models.Task, err error)
	GetTaskList(query bson.M, opts *mongo.FindOptions) (res []models.Task, err error)
	GetTokenById(id primitive.ObjectID) (res *models.Token, err error)
	GetToken(query bson.M, opts *mongo.FindOptions) (res *models.Token, err error)
	GetTokenList(query bson.M, opts *mongo.FindOptions) (res []models.Token, err error)
	GetVariableById(id primitive.ObjectID) (res *models.Variable, err error)
	GetVariable(query bson.M, opts *mongo.FindOptions) (res *models.Variable, err error)
	GetVariableList(query bson.M, opts *mongo.FindOptions) (res []models.Variable, err error)
	GetVariableByKey(key string, opts *mongo.FindOptions) (res *models.Variable, err error)
	GetTaskQueueItemById(id primitive.ObjectID) (res *models.TaskQueueItem, err error)
	GetTaskQueueItem(query bson.M, opts *mongo.FindOptions) (res *models.TaskQueueItem, err error)
	GetTaskQueueItemList(query bson.M, opts *mongo.FindOptions) (res []models.TaskQueueItem, err error)
	GetTaskStatById(id primitive.ObjectID) (res *models.TaskStat, err error)
	GetTaskStat(query bson.M, opts *mongo.FindOptions) (res *models.TaskStat, err error)
	GetTaskStatList(query bson.M, opts *mongo.FindOptions) (res []models.TaskStat, err error)
	GetPluginById(id primitive.ObjectID) (res *models.Plugin, err error)
	GetPlugin(query bson.M, opts *mongo.FindOptions) (res *models.Plugin, err error)
	GetPluginByName(name string) (res *models.Plugin, err error)
	GetPluginList(query bson.M, opts *mongo.FindOptions) (res []models.Plugin, err error)
	GetSpiderStatById(id primitive.ObjectID) (res *models.SpiderStat, err error)
	GetSpiderStat(query bson.M, opts *mongo.FindOptions) (res *models.SpiderStat, err error)
	GetSpiderStatList(query bson.M, opts *mongo.FindOptions) (res []models.SpiderStat, err error)
	GetDataSourceById(id primitive.ObjectID) (res *models.DataSource, err error)
	GetDataSource(query bson.M, opts *mongo.FindOptions) (res *models.DataSource, err error)
	GetDataSourceList(query bson.M, opts *mongo.FindOptions) (res []models.DataSource, err error)
	GetDataCollectionById(id primitive.ObjectID) (res *models.DataCollection, err error)
	GetDataCollection(query bson.M, opts *mongo.FindOptions) (res *models.DataCollection, err error)
	GetDataCollectionList(query bson.M, opts *mongo.FindOptions) (res []models.DataCollection, err error)
	GetDataCollectionByName(name string, opts *mongo.FindOptions) (res *models.DataCollection, err error)
	GetPasswordById(id primitive.ObjectID) (res *models.Password, err error)
	GetPassword(query bson.M, opts *mongo.FindOptions) (res *models.Password, err error)
	GetPasswordList(query bson.M, opts *mongo.FindOptions) (res []models.Password, err error)
	GetExtraValueById(id primitive.ObjectID) (res *models.ExtraValue, err error)
	GetExtraValue(query bson.M, opts *mongo.FindOptions) (res *models.ExtraValue, err error)
	GetExtraValueList(query bson.M, opts *mongo.FindOptions) (res []models.ExtraValue, err error)
	GetExtraValueByObjectIdModel(oid primitive.ObjectID, m string, opts *mongo.FindOptions) (res *models.ExtraValue, err error)
	GetPluginStatusById(id primitive.ObjectID) (res *models.PluginStatus, err error)
	GetPluginStatus(query bson.M, opts *mongo.FindOptions) (res *models.PluginStatus, err error)
	GetPluginStatusList(query bson.M, opts *mongo.FindOptions) (res []models.PluginStatus, err error)
	GetGitById(id primitive.ObjectID) (res *models.Git, err error)
	GetGit(query bson.M, opts *mongo.FindOptions) (res *models.Git, err error)
	GetGitList(query bson.M, opts *mongo.FindOptions) (res []models.Git, err error)
	GetRoleById(id primitive.ObjectID) (res *models.Role, err error)
	GetRole(query bson.M, opts *mongo.FindOptions) (res *models.Role, err error)
	GetRoleList(query bson.M, opts *mongo.FindOptions) (res []models.Role, err error)
	GetRoleByName(name string, opts *mongo.FindOptions) (res *models.Role, err error)
	GetRoleByKey(key string, opts *mongo.FindOptions) (res *models.Role, err error)
	GetUserRoleById(id primitive.ObjectID) (res *models.UserRole, err error)
	GetUserRole(query bson.M, opts *mongo.FindOptions) (res *models.UserRole, err error)
	GetUserRoleList(query bson.M, opts *mongo.FindOptions) (res []models.UserRole, err error)
	GetUserRoleListByUserId(id primitive.ObjectID, opts *mongo.FindOptions) (res []models.UserRole, err error)
	GetUserRoleListByRoleId(id primitive.ObjectID, opts *mongo.FindOptions) (res []models.UserRole, err error)
	GetPermissionById(id primitive.ObjectID) (res *models.Permission, err error)
	GetPermission(query bson.M, opts *mongo.FindOptions) (res *models.Permission, err error)
	GetPermissionList(query bson.M, opts *mongo.FindOptions) (res []models.Permission, err error)
	GetPermissionByKey(key string, opts *mongo.FindOptions) (res *models.Permission, err error)
	GetRolePermission(query bson.M, opts *mongo.FindOptions) (res *models.RolePermission, err error)
	GetRolePermissionList(query bson.M, opts *mongo.FindOptions) (res []models.RolePermission, err error)
	GetRolePermissionListByRoleId(id primitive.ObjectID, opts *mongo.FindOptions) (res []models.RolePermission, err error)
	GetRolePermissionListByPermissionId(id primitive.ObjectID, opts *mongo.FindOptions) (res []models.RolePermission, err error)
}

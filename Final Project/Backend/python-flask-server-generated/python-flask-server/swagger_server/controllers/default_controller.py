import connexion
import six
import json
import firebase_admin
from firebase_admin import credentials
from firebase_admin import db

from swagger_server.models.error import Error  # noqa: E501
from swagger_server.models.project import Project  # noqa: E501
from swagger_server.models.task import Task  # noqa: E501
from swagger_server.models.user import User  # noqa: E501
from swagger_server import util

# Fetch the service account key JSON file contents
cred = credentials.Certificate("mcc-fall-2019-g01-258815-firebase-adminsdk-ywqs5-7688bdf329.json")

# Initialize the app with a service account, granting admin privileges
firebase_admin.initialize_app(cred, {
    "databaseURL": "https://mcc-fall-2019-g01-258815.firebaseio.com/"
})

# As an admin, the app has access to read and write all data, regradless of Security Rules
ref = db.reference()


def get_keywords():  # noqa: E501
    """Get keywords

    Return all different keywords # noqa: E501


    :rtype: List[str]
    """
    req = ref.child("keywords").get()
    result = [key for key in req]
    return result


def get_project_by_id(project_id):  # noqa: E501
    """Get project

    Returns project information # noqa: E501

    :param project_id: ID of project to retrieve
    :type project_id: str

    :rtype: Project
    """
    try:
        res = ref.child("projects/-%s" % project_id).get()
        if res is None:
            res = {}
            # case project id does not exist
    except:
        res = "Failure in inserting users to project id %s and task id %s " % (project_id)

    return res


def get_projects_by_keyword(keyword):  # noqa: E501
    """Finds projects by keyword

    Returns full projects names which correspond to search # noqa: E501

    :param keyword: Content of search
    :type keyword: str

    :rtype: List[str]
    """

    result = []

    try:
        value = ref.child("keywords/%s"%keyword).get()

        if 'project_index' in value:
            for key in value["project_index"]:

                proj = ref.child("projects/-%s"%key).get()
                if 'name' in proj:
                    result.append(proj["name"])
    except:
        result.append({"response":"Failure in getting keywords projects, ID: %s"%keyword})
    return result




    return result


def get_projects_by_name(name):  # noqa: E501
    """Finds projects by name

    Returns full projects names which correspond to search # noqa: E501

    :param name: Content of search
    :type name: str

    :rtype: List[str]
    """

    value = ref.child("projects").get()

    result = []
    for key in value:
        subkey = value[key]
        if 'name' in subkey and  subkey['name'] is name:
            result.append(subkey['name'])
    return result


def get_projects_by_username(username):  # noqa: E501
    """Get projects of user

    Return Full projects information for one user # noqa: E501

    :param username: Username
    :type username: str

    :rtype: List[Project]
    """
    result = []
    try:
        value = ref.child("users/%s"%username).get()

        if 'project_index' in value:
            for key in value["project_index"]:

                proj = ref.child("projects/%s"%key).get()
                if proj is None:
                    continue
                else:
                    proj["project_id"] = key
                    result.append(proj)
    except:
        result.append({"response":"Failure in getting users projects, ID: %s"%username})
    return result


def get_user_by_username(username):  # noqa: E501
    """Get user

    Return user information # noqa: E501

    :param username: Username
    :type username: str

    :rtype: User
    """

    res = ref.child("users/%s"%username)
    if res is None:
        res = {}
    return  res


def project_by_project_id_and_task_id_put(project_id, task_id):  # noqa: E501
    """project_project_id_task_id_put

    Assign a task to a user # noqa: E501
    :param project_id: ID of project to delete
    :type project_id: str
    :param task_id: ID of task to be updated
    :type task_id: str

    :rtype: None
    """
    try:
        if connexion.request.is_json:
            user_list = connexion.request.get_json()
            for key in user_list:
                user = User.from_dict(key)
                value_user = {user.username :  True}
                ref.child("projects/-%s/task_user/-%s" %(project_id, task_id)).update(value_user)

                value_project = {task_id: True}
                ref.child("projects/-%s/user_task/%s" %(project_id, user.username)).update(value_project)
            res = {"response":"Successful inserted %s user to task %s  in project %s " % (len(user_list), task_id,  project_id)}
        else:
            res = {"response":"Invalid Json body"}
    except:
        res = {"response":"Failure in inserting users to project id %s and task id %s " %(project_id, task_id)}

    return res


def project_by_project_id_delete(project_id):  # noqa: E501
    """project_project_id_delete

    Delete a project # noqa: E501

    :param project_id: ID of project to delete
    :type project_id: str

    :rtype: None

    """
    try:

        user_ids = ref.child("projects/-%s/users_index"%project_id).get()
        # delete reference to project from user
        if user_ids:
            for user in user_ids:
                ref.child("users/%s/project_index/-%s"%(user, project_id)).delete()

        tasks = ref.child("projects/-%s/tasks"%project_id).get()
        if tasks:
            for task in tasks:
                ref.child("tasks/%s/tasks"%task).delete()


        keywords = ref.child("projects/-%s/keywords"%project_id).get()
        if keywords:
            for key in keywords:
                ref.child("keywords/%s/project_index/-%s"%(key, project_id)).delete()

        ref.child("projects/-%s"%project_id).delete()
        res = 'Deletion of project: %s was successful'%project_id
    except Exception as ex:
        print(ex)
        res = 'Failure during project deletion'
    return {"response":res}


def project_by_project_id_post(project_id, body=None):  # noqa: E501
    """project_project_id_post

    Create a task # noqa: E501

    :param project_id: ID of project to delete
    :type project_id: str
    :param body: 
    :type body: dict | bytes

    :rtype: str
    """

    try:
        if connexion.request.is_json:
            body = connexion.request.get_json()# noqa: E501
            task_ref = ref.child('tasks').push()
            task_ref.set(body)
            #task = Task.from_dict(body)
            task_id = task_ref.key
            value = {task_id:True}
            ref.child('projects/-%s/tasks/'%project_id).update(value)
        else:
            task_id = {"response":"Invalid Json body"}
    except Exception as ex:
        print(ex)
        task_id = {"response":'Failure during task creation in poject %s'%project_id}
    return {"task_id":task_id}


def project_by_project_id_put(project_id, body=None):  # noqa: E501
    """project_project_id_put

    Add members to a project # noqa: E501

    :param project_id: ID of project to delete
    :type project_id: str
    :param body: 
    :type body: List[]

    :rtype: None
    """
    try:
        if connexion.request.is_json:
            user_obj = connexion.request.get_json()
            user_list = user_obj["users"]
            for user in user_list:

                #user = User.from_dict(key)
                value_user = {"-"+project_id:True}
                ref.child("users/%s/project_index"%user["ID"]).update(value_user)

                value_project = {user["ID"]:True}
                ref.child("projects/-%s/users_index"%project_id).update(value_project)
            res = {"response":"Successful inserted %s user to project %s "%(len(user_list), project_id)}
        else:
            res = {"response":"Invalid Json body"}
    except Exception as e:
        print(e)
        res = {"response":"Failure in inserting users to project id %s "%project_id+ "_"+str(e)}

    return res


def project_post(body=None):  # noqa: E501
    """project_post

    Create a project # noqa: E501

    :param body: 
    :type body: dict | bytes

    :rtype: str
    """
    try:
        if connexion.request.is_json:
            project = connexion.request.get_json()  # noqa: E501
            projects_ref = ref.child('projects')
            proj_ref = projects_ref.push()



            proj_id = proj_ref.key
            ## update also new keywords
            if 'keywords' in project:
                for key in project['keywords']:
                    update_value = {proj_id:True}
                    ref.child('keywords/project_index/%s'%key).update(update_value)
            ref.child('keywords')
            user_index = {}
            if "users_index" in project:
                for key in project["users_index"]:
                    user_index[key] = True
                    update_value = {proj_id:True}
                    ref.child('users/%s/project_index'%key).update(update_value)
            project["users_index"] =user_index
            proj_ref.set(project)
        else:
            proj_id = {"response":"Invalid Json body"}
    except Exception as ex:
        print(ex)
        proj_id = 'Project post failure'
    return {"project_id":proj_id}


def task_by_task_id_put(task_id):  # noqa: E501
    """task_task_id_put

    Update a task # noqa: E501

    :param task_id: ID of task to be updated
    :type task_id: str
    :rtype: None

    """
    try:
        if connexion.request.is_json:
            task_status = connexion.request.get_json()
            ref.child("tasks/-%s"%task_id).update(task_status)
            res = 'Updated task_id %s successful'%task_id
        else:
            res = "Invalid Json body"
    except Exception as ex:
        print(ex)
        res = 'Failure during updating task_id: %s' % task_id
    return res

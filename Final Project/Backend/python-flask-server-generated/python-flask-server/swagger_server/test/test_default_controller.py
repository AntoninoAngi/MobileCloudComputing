# coding: utf-8

from __future__ import absolute_import

from flask import json
from six import BytesIO

from swagger_server.models.error import Error  # noqa: E501
from swagger_server.models.project import Project  # noqa: E501
from swagger_server.models.task import Task  # noqa: E501
from swagger_server.models.user import User  # noqa: E501
from swagger_server.test import BaseTestCase


class TestDefaultController(BaseTestCase):
    """DefaultController integration test stubs"""

    def test_get_keywords(self):
        """Test case for get_keywords

        Get keywords
        """
        response = self.client.open(
            '//keywords',
            method='GET',
            content_type='application/json')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_get_project_by_id(self):
        """Test case for get_project_by_id

        Get project
        """
        response = self.client.open(
            '//project/{project_id}'.format(project_id='project_id_example'),
            method='GET',
            content_type='application/json')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_get_projects_by_keyword(self):
        """Test case for get_projects_by_keyword

        Finds projects by keyword
        """
        response = self.client.open(
            '//searchProjectByKeyword/{keyword}'.format(keyword='keyword_example'),
            method='GET',
            content_type='application/json')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_get_projects_by_name(self):
        """Test case for get_projects_by_name

        Finds projects by name
        """
        response = self.client.open(
            '//searchProjectByName/{name}'.format(name='name_example'),
            method='GET',
            content_type='application/json')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_get_projects_by_username(self):
        """Test case for get_projects_by_username

        Get projects of user
        """
        response = self.client.open(
            '//projects/{username}'.format(username='username_example'),
            method='GET',
            content_type='application/json')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_get_user_by_username(self):
        """Test case for get_user_by_username

        Get user
        """
        response = self.client.open(
            '//user/{username}'.format(username='username_example'),
            method='GET',
            content_type='application/json')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_project_by_project_id_and_task_id_put(self):
        """Test case for project_by_project_id_and_task_id_put

        project_project_id_task_id_put
        """
        response = self.client.open(
            '//project/{project_id}/{task_id}'.format(project_id='project_id_example', task_id='task_id_example'),
            method='PUT',
            content_type='application/json')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_project_by_project_id_delete(self):
        """Test case for project_by_project_id_delete

        project_project_id_delete
        """
        response = self.client.open(
            '//project/{project_id}'.format(project_id='project_id_example'),
            method='DELETE',
            content_type='application/json')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_project_by_project_id_post(self):
        """Test case for project_by_project_id_post

        project_project_id_post
        """
        body = Task()
        response = self.client.open(
            '//project/{project_id}'.format(project_id='project_id_example'),
            method='POST',
            data=json.dumps(body),
            content_type='application/json')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_project_by_project_id_put(self):
        """Test case for project_by_project_id_put

        project_project_id_put
        """
        body = [List[str]()]
        response = self.client.open(
            '//project/{project_id}'.format(project_id='project_id_example'),
            method='PUT',
            data=json.dumps(body),
            content_type='application/json')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_project_post(self):
        """Test case for project_post

        project_post
        """
        body = Project()
        response = self.client.open(
            '//project',
            method='POST',
            data=json.dumps(body),
            content_type='application/json')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_task_by_task_id_put(self):
        """Test case for task_by_task_id_put

        task_task_id_put
        """
        response = self.client.open(
            '//task/{task_id}'.format(task_id='task_id_example'),
            method='PUT',
            content_type='application/json')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))


if __name__ == '__main__':
    import unittest
    unittest.main()

import vantage6.client

IMAGE = 'harbor.carrier-mu.src.surf-hosted.nl/carrier/health_ri_demo'
NAME = 'health-ri-demo'


class HealthRiDemoClient:

    def __init__(self, client: vantage6.client.Client):
        """

        :param client: Vantage6 client
        """
        self.client = client

    def healthRiDemo(self, collaboration, commodity_node, nodes, requirements):
        return self.client.task.create(collaboration=collaboration,
                                       organizations=[commodity_node],
                                       name=NAME, image=IMAGE, description=NAME,
                                       input={'method': 'health_ri_demo', 'master': True,
                                              'args': [nodes, requirements]})

# -*- coding: utf-8 -*-
#
# Copyright (c) ZeroC, Inc. All rights reserved.
#
#
# Ice version 3.7.10
#
# <auto-generated>
#
# Generated from file `ServiceDefinition.ice'
#
# Warning: do not edit this file.
#
# </auto-generated>
#

from sys import version_info as _version_info_
import Ice, IcePy

# Start of module ServiceDefinition
_M_ServiceDefinition = Ice.openModule('ServiceDefinition')
__name__ = 'ServiceDefinition'

_M_ServiceDefinition._t_Service = IcePy.defineValue('::ServiceDefinition::Service', Ice.Value, -1, (), False, True, None, ())

if 'ServicePrx' not in _M_ServiceDefinition.__dict__:
    _M_ServiceDefinition.ServicePrx = Ice.createTempClass()
    class ServicePrx(Ice.ObjectPrx):

        def performOperation(self, input, context=None):
            return _M_ServiceDefinition.Service._op_performOperation.invoke(self, ((input, ), context))

        def performOperationAsync(self, input, context=None):
            return _M_ServiceDefinition.Service._op_performOperation.invokeAsync(self, ((input, ), context))

        def begin_performOperation(self, input, _response=None, _ex=None, _sent=None, context=None):
            return _M_ServiceDefinition.Service._op_performOperation.begin(self, ((input, ), _response, _ex, _sent, context))

        def end_performOperation(self, _r):
            return _M_ServiceDefinition.Service._op_performOperation.end(self, _r)

        def getInvocationCount(self, context=None):
            return _M_ServiceDefinition.Service._op_getInvocationCount.invoke(self, ((), context))

        def getInvocationCountAsync(self, context=None):
            return _M_ServiceDefinition.Service._op_getInvocationCount.invokeAsync(self, ((), context))

        def begin_getInvocationCount(self, _response=None, _ex=None, _sent=None, context=None):
            return _M_ServiceDefinition.Service._op_getInvocationCount.begin(self, ((), _response, _ex, _sent, context))

        def end_getInvocationCount(self, _r):
            return _M_ServiceDefinition.Service._op_getInvocationCount.end(self, _r)

        @staticmethod
        def checkedCast(proxy, facetOrContext=None, context=None):
            return _M_ServiceDefinition.ServicePrx.ice_checkedCast(proxy, '::ServiceDefinition::Service', facetOrContext, context)

        @staticmethod
        def uncheckedCast(proxy, facet=None):
            return _M_ServiceDefinition.ServicePrx.ice_uncheckedCast(proxy, facet)

        @staticmethod
        def ice_staticId():
            return '::ServiceDefinition::Service'
    _M_ServiceDefinition._t_ServicePrx = IcePy.defineProxy('::ServiceDefinition::Service', ServicePrx)

    _M_ServiceDefinition.ServicePrx = ServicePrx
    del ServicePrx

    _M_ServiceDefinition.Service = Ice.createTempClass()
    class Service(Ice.Object):

        def ice_ids(self, current=None):
            return ('::Ice::Object', '::ServiceDefinition::Service')

        def ice_id(self, current=None):
            return '::ServiceDefinition::Service'

        @staticmethod
        def ice_staticId():
            return '::ServiceDefinition::Service'

        def performOperation(self, input, current=None):
            raise NotImplementedError("servant method 'performOperation' not implemented")

        def getInvocationCount(self, current=None):
            raise NotImplementedError("servant method 'getInvocationCount' not implemented")

        def __str__(self):
            return IcePy.stringify(self, _M_ServiceDefinition._t_ServiceDisp)

        __repr__ = __str__

    _M_ServiceDefinition._t_ServiceDisp = IcePy.defineClass('::ServiceDefinition::Service', Service, (), None, ())
    Service._ice_type = _M_ServiceDefinition._t_ServiceDisp

    Service._op_performOperation = IcePy.Operation('performOperation', Ice.OperationMode.Normal, Ice.OperationMode.Normal, False, None, (), (((), IcePy._t_string, False, 0),), (), ((), IcePy._t_string, False, 0), ())
    Service._op_getInvocationCount = IcePy.Operation('getInvocationCount', Ice.OperationMode.Normal, Ice.OperationMode.Normal, False, None, (), (), (), ((), IcePy._t_int, False, 0), ())

    _M_ServiceDefinition.Service = Service
    del Service

# End of module ServiceDefinition

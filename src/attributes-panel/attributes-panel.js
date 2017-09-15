'use strict';

/*@ngInject*/
function OperationAttributes($rootScope, AttributesPanelService ,config) {
  function setCorrectHeight (container) {
    let heightOfOthers = _.reduce(jQuery(
      '> .ibox-title--main, > .c-attributes-tabs',
      container
    ).map((index, el) => $(el).outerHeight(true)), (prev, next) => {
      return prev + next;
    });
    let body = container.querySelector('.ibox-content');

    angular.element(body).css('height', 'calc(100% - ' + heightOfOthers + 'px)');
  }

  return {
    restrict: 'E',
    scope: {
      node: '=',
      disabledMode: '=',
      predefColors: '='
    },
    templateUrl: 'attributes-panel/attributes-panel.html',
    replace: true,
    link: (scope, element) => {
      scope.selected = 'parameters';
      scope.$watch('node', function() {
        scope.$applyAsync(setCorrectHeight.bind(null, element[0]));
      });

      scope.$watch('disabledMode', function() {
        if (scope.disabledMode) {
          AttributesPanelService.setDisabledMode();
          AttributesPanelService.disableElements(element[0]);
        } else {
          AttributesPanelService.enableElements(element[0]);
          AttributesPanelService.setEnabledMode();
        }

        scope.$applyAsync(setCorrectHeight.bind(null, element[0]));
      });
    },

    controller: function ($scope, $element, $timeout, $uibModal) {
      this.getDocsHost = () => config.docsHost;
      this.showErrorMessage = function showErrorMessage () {
        $scope.modal = $uibModal.open({
          scope: $scope,
          template: `
            <button type="button" class="close" aria-label="Close" ng-click="modal.close()">
              <span aria-hidden="true">&times;</span>
            </button>
            <h2>
              Error title:
            </h2>
            <pre class="o-error-trace">{{::node.state.error.title || 'No title'}}</pre>
            <h2>Error message:</h2>
            <pre class="o-error-trace">{{::node.state.error.message || 'No message'}}</pre>
            <div ng-if="::node.state.error.details.stacktrace">
              <h2>Stack trace:</h2>
              <pre class="o-error-trace">{{::node.state.error.details.stacktrace}}</pre>
            </div>
            <button type="button" class="btn btn-default pull-right" ng-click="modal.close()">
              Close
            </button>
            <br style="clear: right;" />`,
          windowClass: 'o-modal--error'
        });
      };

      this.customNameSaved = function() {
        $rootScope.$applyAsync(() => {
          $rootScope.$broadcast('AttributesPanel.UPDATED');
        });
      };
    },
    controllerAs: 'controller'
  };
}

angular.module('deepsense.attributes-panel').
    directive('deepsenseOperationAttributes', OperationAttributes);
